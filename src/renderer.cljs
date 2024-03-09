(ns renderer
  (:require ["@mui/material/Button" :default Button]
            ["@mui/material/Grid" :default Grid]
            ["@mui/material/TextField" :default TextField]
            ["@mui/material/ToggleButton" :default ToggleButton]
            ["@mui/material/ToggleButtonGroup" :default ToggleButtonGroup]
            ["address/promises" :as address]
            [ajax.core :refer [POST]]
            [app-root-path]
            [applied-science.js-interop :as j]
            [child_process]
            [cljs-node-io.core :refer [copy delete-file make-parents slurp
                                       spit]]
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
            [utils]
            [yaml]))

;; https://stackoverflow.com/a/73265958
;; https://clojureverse.org/t/use-esm-with-node-shadow-cljs/9363/4
;; https://github.com/sindresorhus/fix-path/issues/19#issuecomment-1953641218
(def fix-path
  (fix-esm/require "fix-path"))

;; `secrets` is kept separate from `state` to specifically handle sensitive information
;; This separation ensures that changes to `secrets` can be directly
;; synchronized with `secrets.yaml`, allowing for a consistent way to update
;; and store sensitive configurations separately from the application's general state.
(defonce secrets (reagent/atom {:key ""}))

(defonce config
  (reagent/atom {}))

(defonce state
  (reagent/atom {:manual false
                 :open false
                 :mics []}))

;; The core.async channel and go-loop are used to manage the asynchronous processing
;; of audio chunks. This ensures that updates to the application state are serialized,
;; preventing concurrent state modifications that could lead to inconsistencies.
(defonce audio-channel
  (async/chan))

(def sample-rate 16000)

(defn find-device-id [devices label]
  (->> devices
       js->clj
       (filter (fn [device]
                 (= (.-label device) label)))
       first
       .-deviceId))

;; This channel allows the application
;; to asynchronously process microphone change events, ensuring that updates to the
;; application's state regarding the current microphone are serialized. This serialization
;; prevents concurrent state modifications that could lead to inconsistencies.
(defonce mic-channel
  (async/chan))

(defn create-context []
  (js/AudioContext. (clj->js {:sampleRate sample-rate})))

(defn record []
  (async/go-loop [loop-state {}]
    (let [mic (async/<! mic-channel)
          mic* (if (empty? mic)
                 nil
                 mic)]
      (if (= (:mic loop-state) mic*)
        (recur loop-state)
        (do (when (:context loop-state)
              (js/console.log "Closing existing audio context.")
              (.close (:context loop-state)))
            (if mic*
              (let [_ (<p! (js/navigator.mediaDevices.getUserMedia (clj->js {:audio true})))
                    devices (<p! (js/navigator.mediaDevices.enumerateDevices))
                    media (<p! (-> {:audio {:deviceId {:exact (find-device-id devices mic*)}}}
                                   clj->js
                                   js/navigator.mediaDevices.getUserMedia))
                    context (create-context)
                    _ (<p! (.audioWorklet.addModule context "audio.js"))
                    processor (js/AudioWorkletNode. context "processor")]
                (.connect (.createMediaStreamSource context media) processor)
                (j/assoc-in! processor [:port :onmessage] (fn [message]
                                                            (async/put! audio-channel message.data)))
                (recur {:mic mic*
                        :context context}))
              (recur {})))))))

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

;; https://github.com/microsoft/vscode-docs/blob/a89ef7fa002d0eaed7f80661525294ee55c40c73/docs/editor/command-line.md?plain=1#L71
(def line
;; This is an arbitrarily high number chosen to ensure that when a file is opened, it displays starting from the last line.
  10000)

(defn open-transcription
  [transcription-filepath]
  (child_process/spawn "code" (clj->js ["-g" (str transcription-filepath ":" line)])))

(defn generate-text-filename []
  (str (random-uuid) ".txt"))

(defn generate-text-path []
  (path/join app-temp-directory (generate-text-filename)))

(defn handler [transcription-filepath response]
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
        text-path (generate-text-path)]
    (when-not (empty? transcription-text)
      (make-parents transcription-filepath)
      (if (fs/existsSync transcription-filepath)
        (do (fs/copyFileSync transcription-filepath text-path)
            (fs/chmodSync text-path 0200)
            (spit text-path (str "\n\n" transcription-text) :append true))
        (spit text-path transcription-text))
      (fs/chmodSync text-path 0400)
      (fs/renameSync text-path transcription-filepath)
      (when (:open @state)
        (utils/setval [specter/ATOM :open] false state)
        (open-transcription transcription-filepath)))))

(defn create-readable []
  (let [readable (stream/Readable. (clj->js {:read (fn [])}))
        filepath (generate-audio-path)
        ffmpeg (child_process/spawn "ffmpeg" (clj->js ["-f" "f32le" "-ar" sample-rate "-i" "pipe:0" "-b:a" "24k" filepath]))]
    (.pipe readable ffmpeg.stdin)
    (.on ffmpeg "close" (fn []
                          (js/console.log "ffmpeg process closed")
                          (POST url {:handler (partial handler (generate-transcription-filepath))
                                     :headers {:Content-Type "audio/*"
                                               :Authorization (str "Token " (:key @secrets))}
                                     :body (fs/readFileSync filepath)
                                     :response-format :json
                                     :keywords? true})
                          (delete-file filepath)))
    readable))

(defn update-mac []
  (js/console.log "Updating MAC address in application state")
  (js-await [mac (address/mac)]
    (utils/setval [specter/ATOM :mac] mac state)))

(defn merge-into-atom
  [map* atom*]
  (specter/transform specter/ATOM
                     (fn [value]
                       (merge value map*))
                     atom*))

(defn handle-shortcut []
  (js/console.log "Shortcut pressed")
  (merge-into-atom {:manual true
                    :open true}
                   state)
  (when (fs/existsSync transcription-directory-path)
    (js-await [transcription-files (recursive transcription-directory-path)]
      (let [transcription-files* (js->clj transcription-files)]
        (when (not-empty transcription-files*)
          (open-transcription (last (sort transcription-files*))))))))

(defn get-mic-labels [devices]
  (->> devices
       js->clj
       (filter (fn [device]
                 (and (= "audioinput" (.-kind device))
                      (not= (.-deviceId device) "default"))))
       (map (fn [device]
              (.-label device)))))

(defn update-mics []
  (js/console.log "Attempting to update microphone list")
  (js-await [_ (js/navigator.mediaDevices.getUserMedia (clj->js {:audio true}))]
    (js-await [devices (js/navigator.mediaDevices.enumerateDevices)]
      (utils/setval [specter/ATOM :mics] (get-mic-labels devices) state))))

(defn get-path [filename]
  (path/join (os/homedir) ".config/say" filename))

(defn sync-settings
  [filename atom*]
  (let [path* (get-path filename)]
    (when (fs/existsSync path*)
      (-> path*
          slurp
          yaml/parse
          (js->clj :keywordize-keys true)
          (merge-into-atom atom*)))
    (add-watch atom* :change (fn [_ _ _ value]
                               (js/console.log "Settings atom value updated, writing to file")
                               (spit path* (yaml/stringify (clj->js value)))))))

(def secrets-filename "secrets.yaml")

(def config-filename "config.yaml")

(defn is-built-in [label]
  (and (str/ends-with? label "(Built-in)")
       (not (str/includes? label "External"))))

(defn select-mic [map*]
  (cond
    (or (nil? (:hideaway map*)) (= (:hideaway map*) (:mac map*))) (->> map*
                                                                       :mics
                                                                       (filter is-built-in)
                                                                       first)
    (:mic map*) (some #{(:mic map*)} (:mics map*))))

(defn update-mic []
;; An empty string "" is used as a fallback to avoid "Can't put nil on a channel" error.
  (async/put! mic-channel (or (select-mic (merge @secrets @config @state)) "")))

;; Using defonce to ensure the root is only created once. This prevents warnings about
;; calling ReactDOMClient.createRoot() on a container that has already been passed to
;; createRoot() before during hot reloads or re-evaluations of the code.
(defonce root
  (client/create-root (js/document.getElementById "app")))

(defn key-field []
  [:> TextField {:label "Deepgram API Key"
                 :type "password"
                 :value (:key @secrets)
                 :on-change (fn [event]
                              (utils/setval [specter/ATOM :key] event.target.value secrets))
                 :full-width true}])

(defn hideaway-button []
  (let [secrets* @secrets
        state* @state]
    [:> Button {:variant (if (or (:hideaway secrets*) (:mac state*))
                           "contained"
                           "disabled")
                :on-click (fn []
                            (utils/setval [specter/ATOM :hideaway]
                                          (if (:hideaway secrets*)
                                            specter/NONE
                                            (:mac state*))
                                          secrets)
                            (update-mic))
                :full-width true}
     (if (:hideaway secrets*)
       "DISABLE HIDEAWAY"
       "ENABLE HIDEAWAY")]))

(defn mic-toggle-buttons []
  [:> ToggleButtonGroup
   {:value (:mic @config)
    :exclusive true
    :on-change (fn [_ value]
                 (utils/setval [specter/ATOM :mic] value config)
                 (update-mic))
    :full-width true
    :orientation "vertical"}
   (->> @state
        :mics
        (utils/setval specter/AFTER-ELEM (:mic @config))
        (apply sorted-set)
        (map (fn [mic]
               [:> ToggleButton
                {:value mic
                 :key mic
                 :sx {:text-transform "none"}}
                mic])))])

(defn grid []
  [:> Grid {:container true
            :p 2
            :spacing 2}
   [:> Grid {:item true
             :xs 12}
    [key-field]]
   [:> Grid {:item true
             :xs 12}
    [hideaway-button]]
   [:> Grid {:item true
             :xs 12}
    [mic-toggle-buttons]]])

(def plist-filename
  "say.plist")

(def source-path
  (path/join (app-root-path/toString) plist-filename))

(def launch-agents-path
  (path/join (os/homedir) "Library/LaunchAgents"))

(def target-path
  (path/join launch-agents-path plist-filename))

(def source-content
  (slurp source-path))

(defn get-target-content
  []
  (when (fs/existsSync target-path)
    (slurp target-path)))

(defn update-plist
  []
  (when (not= source-content (get-target-content))
    (copy source-path target-path)))

(def network-path
  (path/join (app-root-path/toString) "network.sh"))

(defn after-load []
  (js/console.log "Executing after-load function")
;; Using fix-path to ensure the system PATH is correctly set in the Electron environment. This resolves the "spawn ffmpeg ENOENT" error by making sure ffmpeg can be found and executed.
  ((.-default fix-path))
  (sync-settings secrets-filename secrets)
  (sync-settings config-filename config)
  (client/render root [grid])
  (electron/ipcRenderer.on channel handle-shortcut)
;; This section is tasked with the process of determining the active microphone,
;; The essential aspect is ensuring that certain operations precede the determination of the active microphone:
;; 1. Updating the MAC address and updating the list of available microphones are prerequisites.
;; 2. Setting the active microphone based on the updated MAC address and microphone list.
  (js-await [_ (update-mac)]
    (js-await [_ (update-mics)]
      (.stdout.on (child_process/spawn "expect" (clj->js [network-path]))
                  "data"
                  (fn []
                    (js-await [_ (update-mac)]
                      (update-mic))))
      (set! js/navigator.mediaDevices.ondevicechange (fn []
                                                       (js-await [_ (update-mics)]
                                                         (update-mic))))
      (update-mic)))
  (update-plist))

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
    (async/go-loop [loop-state {:readable (create-readable)
                                :readable-length 0
                                :pad []
                                :pause-length 0
                                :raw []
                                :vad false
                                :h tensor
                                :c tensor}]
      (let [combined (concat (:raw loop-state) (async/<! audio-channel))
            loop-state* (merge loop-state
;; https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
                               (if (< (count combined) window-size-samples)
                                 {:raw combined}
                                 (let [before (take window-size-samples combined)
                                       input (ort/Tensor. (js/Float32Array. before) (clj->js [1 (count before)]))
                                       result (js->clj (->> {:input input
                                                             :sr sr}
                                                            (merge loop-state)
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
                                            (merge {:pause-length (+ (:pause-length loop-state) (count before))}
                                                   (if (and (<= (:pause-length loop-state) samples-in-pause) (:vad loop-state))
                                                     (do (push (:readable loop-state) before)
                                                         {:readable-length (+ (:readable-length loop-state) (count before))})
                                                     {:pad (take-last samples-in-pause (concat (:pad loop-state) before))}))
                                            (let [padded (concat (:pad loop-state) before)]
                                              (push (:readable loop-state) padded)
                                              {:readable-length (+ (:readable-length loop-state) (count padded))
                                               :pad []
                                               :pause-length 0
                                               :vad true}))))))]
        (recur (merge loop-state* (if (or (:manual @state) (and (< samples-in-readable (:readable-length loop-state*))
                                                                (< samples-in-pause (:pause-length loop-state*))))
                                    (do (js/console.log "Current stream length:" (:readable-length loop-state*))
                                        (utils/setval [specter/ATOM :manual] false state)
                                        (push (:readable loop-state*) (:raw loop-state*))
                                        (.push (:readable loop-state*) nil)
                                        {:readable (create-readable)
                                         :readable-length 0
                                         :pad []
                                         :pause-length 0
                                         :raw []
                                         :vad false})
                                    {})))))))

(defn init []
  (after-load)
;; The `record` function initiates the audio recording process. It's called once to prevent
;; excessive pending puts on the channel when called multiple times,
;; which would exceed the limit of 1024.
  (record)
;; The `process` function starts a go-loop for processing audio data. Calling it once here
;; prevents multiple instances of the go-loop from being created, which would otherwise
;; attempt to consume audio data simultaneously. This concurrent consumption could lead to
;; corrupted audio files as multiple processes interfere with each other.
  (process))