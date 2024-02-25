(ns renderer
  (:require ["@mui/material/TextField" :default TextField]
            [applied-science.js-interop :as j]
            [cljs-node-io.core :refer [slurp spit]]
            [cljs.core.async :as async]
            [cljs.core.async.interop :refer-macros [<p!]]
            [com.rpl.specter :as specter]
            [reagent.core :as reagent]
            [reagent.dom.client :as client]
            [shadow.cljs.modern :refer [js-await]]
            [yaml :as yaml]))

;; Using js/require to directly require Node.js modules like "os" and "path" because
;; they are not available in the browser environment by default. The ClojureScript
;; ns form and :require cannot be used for Node.js built-in modules in non-Node
;; environments.
(def os
  (js/require "os"))

(def path
  (js/require "path"))

(def fs
  (js/require "fs"))

(def ort
  ;; https://github.com/microsoft/onnxruntime/issues/11181#issuecomment-1733461246
  ;; Using js/require to load onnxruntime-node due to an error encountered when
  ;; attempting to use the ClojureScript :require syntax. The error is as follows:
  ;; "Module not provided: ../bin/napi-v3/undefined/undefined/onnxruntime_binding.node",
  ;; which prevents successful module resolution by Shadow CLJS.
  (js/require "onnxruntime-node"))

;; Using defonce to ensure the root is only created once. This prevents warnings about
;; calling ReactDOMClient.createRoot() on a container that has already been passed to
;; createRoot() before during hot reloads or re-evaluations of the code.
(defonce root
  (client/create-root (js/document.getElementById "app")))

(defonce secrets (reagent/atom {:key ""}))

(def secrets-path (path.join (os.homedir) ".config/say/secrets.yaml"))

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
  (js-await [stream (js/navigator.mediaDevices.getUserMedia (clj->js {:audio true}))]
    (js-await [_ (.audioWorklet.addModule context "audio.js")]
      (let [processor (js/AudioWorkletNode. context "processor")]
        (.connect (.createMediaStreamSource context stream) processor)
        (j/assoc-in! processor [:port :onmessage] (fn [message]
                                                    (async/put! chan message.data)))))))

(def shape
  [2 1 64])

(def tensor
  (ort.Tensor. (js/Float32Array. (apply * shape)) (clj->js shape)))

(defonce state
  (atom {:stream-length 0
         :pad []
         :pause-length 0
         :raw []
         :vad false
         :h tensor
         :c tensor}))

(defn load []
  (js/console.log "Hello, Renderer!")
  (when (fs.existsSync secrets-path)
    (reset! secrets (js->clj (yaml/parse (slurp secrets-path)) :keywordize-keys true)))
  (client/render root [api-key])
  (add-watch secrets :change (fn [_ _ _ secrets*]
                               (js/console.log "Secrets updated")
                               (spit secrets-path (yaml/stringify (clj->js secrets*))))))

;; https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L207
(def window-size-samples
  1536)

(def sr
  (ort.Tensor. (js/BigInt64Array. [(js/BigInt sample-rate)])))

(def pause-duration 1.5)

(def samples-in-pause (* sample-rate pause-duration))

(def stream-duration 60)

(def samples-in-stream (* sample-rate stream-duration))

(defn save []
  (let [state* @state]
    (js/console.log "Current stream length:" (:stream-length state*))
    (specter/setval specter/ATOM
                    (merge state* {:stream-length 0
                                   :pad []
                                   :pause-length 0
                                   :raw []
                                   :vad false})
                    state)))

(defn process []
  (js-await [session (ort.InferenceSession.create "vad.onnx")]
    (async/go-loop []
      (let [state* @state
            combined (concat (:raw state*) (async/<! chan))]
        ;; https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
        (if (< (count combined) window-size-samples)
          (specter/setval [specter/ATOM :raw] combined state)
          (let [before (take window-size-samples combined)
                input (ort.Tensor. (js/Float32Array. before) (clj->js [1 (count before)]))
                result (js->clj (->> {:input input
                                      :sr sr}
                                     (merge state*)
                                     clj->js
                                     (.run session)
                                     <p!)
                                :keywordize-keys true)]
            (specter/setval specter/ATOM
                            (merge state*
                                   {:raw (drop window-size-samples combined)
                                    :h (:hn result)
                                    :c (:cn result)}
                                   (if (-> result
                                           :output
                                           .-data
                                           first
                                           (<= 0.5))
                                     (merge {:pause-length (+ (:pause-length state*) (count before))}
                                            (if (and (<= (:pause-length state*) samples-in-pause) (:vad state*))
                                              {:stream-length (+ (:stream-length state*) (count before))}
                                              {:pad (take-last samples-in-pause (concat (:pad state*) before))}))
                                     {:stream-length (+ (:stream-length state*) (count (:pad state*)) (count before))
                                      :pad []
                                      :pause-length 0
                                      :vad true}))
                            state)
            (let [state** @state]
              (when (and (< samples-in-stream (:stream-length state**))
                         (< samples-in-pause (:pause-length state**)))
                (save)))))
        (recur)))))

(defn init []
  (load)
  (record)
  (process))

(defonce temp-directory
  (os.tmpdir))

(defonce app-temp-directory
  (fs.mkdtempSync (str temp-directory "/say-")))

(defn generate-filename []
  (str (random-uuid) ".opus"))

(defn generate-filepath []
  (path.join app-temp-directory (generate-filename)))