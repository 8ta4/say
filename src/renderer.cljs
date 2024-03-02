(ns renderer
  (:require ["@mui/material/Button" :default Button]
            ["@mui/material/Grid" :default Grid]
            ["@mui/material/TextField" :default TextField]
            ["address/promises" :as address]
            [ajax.core :refer [POST]]
            [app-root-path]
            [applied-science.js-interop :as j]
            [child_process]
            [cljs-node-io.core :refer [make-parents slurp spit]]
            [cljs.core.async :as async]
            [cljs.core.async.interop :refer [<p!]]
            [clojure.string :as str]
            [com.rpl.specter :as specter]
            [dayjs]
            [electron]
            [fix-esm]
            [fs]
            [onnxruntime-node :as ort]
            [os]
            [path]
            [reagent.core :as reagent]
            [reagent.dom.client :as client]
            [recursive-readdir :as recursive]
            [shadow.cljs.modern :refer [js-await]]
            [shared :refer [channel]]
            [stream]
            [yaml]))

;; https://stackoverflow.com/a/73265958
;; https://clojureverse.org/t/use-esm-with-node-shadow-cljs/9363/4
;; https://github.com/sindresorhus/fix-path/issues/19#issuecomment-1953641218
(def fix-path
  (fix-esm/require "fix-path"))

;; Using defonce to ensure the root is only created once. This prevents warnings about
;; calling ReactDOMClient.createRoot() on a container that has already been passed to
;; createRoot() before during hot reloads or re-evaluations of the code.
(defonce root
  (client/create-root (js/document.getElementById "app")))

(defonce secrets (reagent/atom {:key ""}))

(def secrets-path (path/join (os/homedir) ".config/say/secrets.yaml"))

(defn toggle-hideaway []
  (if (:hideaway @secrets)
    (specter/setval [specter/ATOM :hideaway] specter/NONE secrets)
    (js-await [mac (address/mac)]
      (specter/setval [specter/ATOM :hideaway] mac secrets))))

(defn hideaway-button []
  [:> Button {:variant "contained"
              :on-click toggle-hideaway}
   (if (:hideaway @secrets)
     "DISABLE HIDEAWAY"
     "ENABLE HIDEAWAY")])

(defn key-field []
  [:> TextField {:label "Deepgram API Key"
                 :type "password"
                 :value (:key @secrets)
                 :full-width true
                 :on-change (fn [event]
                              (specter/setval [specter/ATOM :key] event.target.value secrets))}])

(defn grid []
  [:> Grid {:container true
            :p 2
            :spacing 2}
   [:> Grid {:item true
             :xs 12}
    [key-field]]
   [:> Grid {:item true
             :xs 12}
    [hideaway-button]]])

;; The core.async channel and go-loop are used to manage the asynchronous processing
;; of audio chunks. This ensures that updates to the application state are serialized,
;; preventing concurrent state modifications that could lead to inconsistencies.
(defonce chan
  (async/chan))

(def sample-rate 16000)

(defonce context
  (js/AudioContext. (clj->js {:sampleRate sample-rate})))

(defn find-device-id [devices]
  (->> devices
       js->clj
       (filter (fn [device]
                 (and (str/ends-with? (.-label device) "(Built-in)")
;; Exclude the default device because when an external microphone is unplugged, the recording stops working if the default device is selected.
                      (not= (.-deviceId device) "default")
                      (not (str/includes? (.-label device) "External")))))
       first
       .-deviceId))

(defn record []
  (js-await [_ (js/navigator.mediaDevices.getUserMedia (clj->js {:audio true}))]
    (js-await [devices (js/navigator.mediaDevices.enumerateDevices)]
      (js-await [media (-> {:audio {:deviceId {:exact (find-device-id devices)}}}
                           clj->js
                           js/navigator.mediaDevices.getUserMedia)]
        (js-await [_ (.audioWorklet.addModule context "audio.js")]
          (let [processor (js/AudioWorkletNode. context "processor")]
            (.connect (.createMediaStreamSource context media) processor)
            (j/assoc-in! processor [:port :onmessage] (fn [message]
                                                        (async/put! chan message.data)))))))))

(def shape
  [2 1 64])

(def tensor
  (ort/Tensor. (js/Float32Array. (apply * shape)) (clj->js shape)))

(defonce temp-directory
  (os/tmpdir))

(defonce app-temp-directory
  (fs/mkdtempSync (path/join temp-directory "say-")))

(defn generate-audio-filename []
  (str (random-uuid) ".opus"))

(defn generate-audio-path []
  (path/join app-temp-directory (generate-audio-filename)))

(def url
  "https://api.deepgram.com/v1/listen?model=nova-2&smart_format=true")

(def transcription-directory-path
  (path/join (os/homedir) ".local/share/say"))

(defn generate-transcription-filepath []
  (path/join transcription-directory-path (str (.format (dayjs) "YYYY/MM/DD") ".txt")))

(defonce state
  (atom {:manual false
         :open false}))

;; https://github.com/microsoft/vscode-docs/blob/a89ef7fa002d0eaed7f80661525294ee55c40c73/docs/editor/command-line.md?plain=1#L71
(def line
;; This is an arbitrarily high number chosen to ensure that when a file is opened, it displays starting from the last line.
  10000)

(defn open-transcription
  [transcription-filepath]
  (child_process/spawn "code" (clj->js ["-g" (str transcription-filepath ":" line)])))

(defn handler [response]
  (js/console.log "handler called")
  (let [transcription-text (->> response
                                :results
                                :channels
                                first
                                :alternatives
                                first
                                :paragraphs
                                :paragraphs
                                (mapcat :sentences)
                                (map :text)
                                (str/join "\n"))
        transcription-filepath (generate-transcription-filepath)]
    (when-not (empty? transcription-text)
      (make-parents transcription-filepath)
      (spit transcription-filepath
            (str (if (fs/existsSync transcription-filepath)
                   "\n\n"
                   "")
                 transcription-text)
            :append true)
      (when (:open @state)
        (specter/setval [specter/ATOM :open] false state)
        (open-transcription transcription-filepath)))))

(defn create-readable []
  (let [readable (stream/Readable. (clj->js {:read (fn [])}))
        filepath (generate-audio-path)
        ffmpeg (child_process/spawn "ffmpeg" (clj->js ["-f" "f32le" "-ar" sample-rate "-i" "pipe:0" "-b:a" "24k" filepath]))]
    (.pipe readable ffmpeg.stdin)
    (.on ffmpeg "close" (fn []
                          (js/console.log "ffmpeg process closed")
                          (POST url {:handler handler
                                     :headers {:Content-Type "audio/*"
                                               :Authorization (str "Token " (:key @secrets))}
                                     :body (fs/readFileSync filepath)
                                     :response-format :json
                                     :keywords? true})))
    readable))

(defn update-mac []
  (js-await [mac (address/mac)]
    (specter/setval [specter/ATOM :mac]
                    (if (nil? mac)
                      specter/NONE
                      mac)
                    state)))

(defn handle-shortcut []
  (js/console.log "Shortcut pressed")
  (specter/setval specter/ATOM
                  {:manual true
                   :open true}
                  state)
  (js-await [transcription-files (recursive transcription-directory-path)]
    (let [transcription-files* (js->clj transcription-files)]
      (when (not-empty transcription-files*)
        (open-transcription (last (sort transcription-files*)))))))

(defn load []
  (js/console.log "Hello, Renderer!")

;; Using fix-path to ensure the system PATH is correctly set in the Electron environment. This resolves the "spawn ffmpeg ENOENT" error by making sure ffmpeg can be found and executed.
  ((.-default fix-path))
  (when (fs/existsSync secrets-path)
    (specter/setval specter/ATOM (js->clj (yaml/parse (slurp secrets-path)) :keywordize-keys true) secrets))
  (.stdout.on (child_process/spawn "expect" (clj->js ["network.sh"])) "data" update-mac)
  (client/render root [grid])
  (add-watch secrets :change (fn [_ _ _ secrets*]
                               (js/console.log "Secrets updated")
                               (spit secrets-path (yaml/stringify (clj->js secrets*)))))
  (electron/ipcRenderer.on channel handle-shortcut))

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
  (->> audio
       js/Float32Array.
       .-buffer
       js/Buffer.from
       (.push readable)))

(def vad-path
  (path/join (app-root-path/toString) "vad.onnx"))

(defn process []
  (js-await [session (ort/InferenceSession.create vad-path)]
    (async/go-loop [process-state {:readable (create-readable)
                                   :readable-length 0
                                   :pad []
                                   :pause-length 0
                                   :raw []
                                   :vad false
                                   :h tensor
                                   :c tensor}]
      (let [combined (concat (:raw process-state) (async/<! chan))
            process-state* (merge process-state
;; https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
                                  (if (< (count combined) window-size-samples)
                                    {:raw combined}
                                    (let [before (take window-size-samples combined)
                                          input (ort/Tensor. (js/Float32Array. before) (clj->js [1 (count before)]))
                                          result (js->clj (->> {:input input
                                                                :sr sr}
                                                               (merge process-state)
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
                                               (merge {:pause-length (+ (:pause-length process-state) (count before))}
                                                      (if (and (<= (:pause-length process-state) samples-in-pause) (:vad process-state))
                                                        (do (push (:readable process-state) before)
                                                            {:readable-length (+ (:readable-length process-state) (count before))})
                                                        {:pad (take-last samples-in-pause (concat (:pad process-state) before))}))
                                               (let [padded (concat (:pad process-state) before)]
                                                 (push (:readable process-state) padded)
                                                 {:readable-length (+ (:readable-length process-state) (count padded))
                                                  :pad []
                                                  :pause-length 0
                                                  :vad true}))))))]
        (recur (merge process-state* (if (or (:manual @state) (and (< samples-in-readable (:readable-length process-state*))
                                                                   (< samples-in-pause (:pause-length process-state*))))
                                       (do (js/console.log "Current stream length:" (:readable-length process-state*))
                                           (specter/setval [specter/ATOM :manual] false state)
                                           (push (:readable process-state*) (:raw process-state*))
                                           (.push (:readable process-state*) nil)
                                           {:readable (create-readable)
                                            :readable-length 0
                                            :pad []
                                            :pause-length 0
                                            :raw []
                                            :vad false})
                                       {})))))))

(defn init []
  (load)
  (record)
  (process))
