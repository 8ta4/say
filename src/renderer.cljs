(ns renderer
  (:require ["@mui/material/TextField" :default TextField]
            [applied-science.js-interop :as j]
            [cljs-node-io.core :refer [slurp spit]]
            [cljs.core.async :as async]
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

(def chan
  (async/chan))

(defn record []
  (js-await [stream (js/navigator.mediaDevices.getUserMedia #js {:audio true})]
    (let [context (js/AudioContext. {:sampleRate 16000})]
      (js-await [_ (.audioWorklet.addModule context "audio.js")]
        (let [processor (js/AudioWorkletNode. context "processor")]
          (.connect (.createMediaStreamSource context stream) processor)
          (j/assoc-in! processor [:port :onmessage] (fn [message]
                                                      (async/put! chan message.data))))))))
(defn append-float-32-array
  [x y]
  (let [combined (js/Float32Array. (+ (.-length x)
                                      (.-length y)))]
    (.set combined x)
    (.set combined y (.-length x))
    combined))

(def state
  (atom {:raw (js/Float32Array.)}))

(defn init []
  (js/console.log "Hello, Renderer!")
  (when (fs.existsSync secrets-path)
    (reset! secrets (js->clj (yaml/parse (slurp secrets-path)) :keywordize-keys true)))
  (client/render root [api-key])
  (add-watch secrets :change (fn [_ _ _ secrets*]
                               (js/console.log "Secrets updated")
                               (spit secrets-path (yaml/stringify (clj->js secrets*)))))
  (record)
  (async/go-loop []
    (let [data (async/<! chan)]
      (specter/transform [specter/ATOM :raw] (fn [raw] (append-float-32-array raw data)) state)
      (recur))))

;; https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L207
(def window-size-samples
  1536)