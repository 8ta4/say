(ns renderer
  (:require ["@mui/material/TextField" :default TextField]
            [applied-science.js-interop :as j]
            [child_process]
            [cljs-node-io.core :refer [slurp spit]]
            [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [com.rpl.specter :as specter]
            [electron]
            [fs]
            [onnxruntime-node :as ort]
            [os]
            [path]
            [reagent.core :as reagent]
            [reagent.dom.client :as client]
            [shadow.cljs.modern :refer [js-await]]
            [shared :refer [channel]]
            [stream]
            [yaml]))

;; Using defonce to ensure the root is only created once. This prevents warnings about
;; calling ReactDOMClient.createRoot() on a container that has already been passed to
;; createRoot() before during hot reloads or re-evaluations of the code.
(defonce root
  (client/create-root (js/document.getElementById "app")))

(defonce secrets (reagent/atom {:key ""}))

(def secrets-path (path/join (os/homedir) ".config/say/secrets.yaml"))

(defn api-key []
  [:> TextField {:label "Deepgram API Key"
                 :type "password"
                 :value (:key @secrets)
                 :on-change (fn [event]
                              (specter/setval [specter/ATOM :key] event.target.value secrets))}])

(defonce chan
  (async/chan))

(def sample-rate 16000)

(defonce context
  (js/AudioContext. (clj->js {:sampleRate sample-rate})))

(defn record []
  (js-await [media (js/navigator.mediaDevices.getUserMedia (clj->js {:audio true}))]
    (js-await [_ (.audioWorklet.addModule context "audio.js")]
      (let [processor (js/AudioWorkletNode. context "processor")]
        (.connect (.createMediaStreamSource context media) processor)
        (j/assoc-in! processor [:port :onmessage] (fn [message]
                                                    (async/put! chan message.data)))))))

(def shape
  [2 1 64])

(def tensor
  (ort/Tensor. (js/Float32Array. (apply * shape)) (clj->js shape)))

(defonce temp-directory
  (os/tmpdir))

(defonce app-temp-directory
  (fs/mkdtempSync (str temp-directory "/say-")))

(defn generate-filename []
  (str (random-uuid) ".opus"))

(defn generate-filepath []
  (path/join app-temp-directory (generate-filename)))

(defn create-readable []
  (let [readable (stream/Readable. (clj->js {:read (fn [])}))
        ffmpeg (child_process/spawn "ffmpeg" (clj->js ["-f" "f32le" "-ar" sample-rate "-i" "pipe:0" "-b:a" "24k" (generate-filepath)]))]
    (.pipe readable ffmpeg.stdin)
    (.on ffmpeg "close" (fn [_]
                          (js/console.log "ffmpeg process closed")))
    readable))

(defn load []
  (js/console.log "Hello, Renderer!")
  (when (fs/existsSync secrets-path)
    (reset! secrets (js->clj (yaml/parse (slurp secrets-path)) :keywordize-keys true)))
  (client/render root [api-key])
  (add-watch secrets :change (fn [_ _ _ secrets*]
                               (js/console.log "Secrets updated")
                               (spit secrets-path (yaml/stringify (clj->js secrets*)))))
  (electron/ipcRenderer.on channel (fn []
                                     (js/console.log "Shortcut pressed"))))

;; https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L207
(def window-size-samples
  1536)

(def sr
  (ort/Tensor. (js/BigInt64Array. [(js/BigInt sample-rate)])))

(def pause-duration 1.5)

(def samples-in-pause (* sample-rate pause-duration))

(def stream-duration 60)

(def samples-in-readable (* sample-rate stream-duration))

(defn push [readable audio]
  (.push readable (js/Buffer.from (.-buffer (js/Float32Array. audio)))))

(defn process []
  (js-await [session (ort/InferenceSession.create "vad.onnx")]
    (async/go-loop [foo {:readable (create-readable)
                         :readable-length 0
                         :pad []
                         :pause-length 0
                         :raw []
                         :vad false
                         :h tensor
                         :c tensor}]
      (let [combined (concat (:raw foo) (async/<! chan))
            bar (merge foo
                       ;; https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
                       (if (< (count combined) window-size-samples)
                         {:raw combined}
                         (let [before (take window-size-samples combined)
                               input (ort/Tensor. (js/Float32Array. before) (clj->js [1 (count before)]))
                               result (js->clj (->> {:input input
                                                     :sr sr}
                                                    (merge foo)
                                                    clj->js
                                                    (.run session)
                                                    <p!)
                                               :keywordize-keys true)]
                           (merge {:raw (drop window-size-samples combined)
                                   :h (:hn result)
                                   :c (:cn result)}
                                  (if (-> result
                                          :output
                                          .-data
                                          first
                                          (<= 0.5))
                                    (merge {:pause-length (+ (:pause-length foo) (count before))}
                                           (if (and (<= (:pause-length foo) samples-in-pause) (:vad foo))
                                             (do (push (:readable foo) before)
                                                 {:readable-length (+ (:readable-length foo) (count before))})
                                             {:pad (take-last samples-in-pause (concat (:pad foo) before))}))
                                    (let [combined* (concat (:pad foo) before)]
                                      (push (:readable foo) combined*)
                                      {:readable-length (+ (:readable-length foo) (count combined*))
                                       :pad []
                                       :pause-length 0
                                       :vad true}))))))]
        (recur (merge bar (if (and (< samples-in-readable (:readable-length bar))
                                   (< samples-in-pause (:pause-length bar)))
                            (do
                              (js/console.log "Current stream length:" (:readable-length bar))
                              (push (:readable bar) (:raw bar))
                              {:readable-length 0
                               :pad []
                               :pause-length 0
                               :raw []
                               :vad false})
                            {})))))))

(defn init []
  (load)
  (record)
  (process))
