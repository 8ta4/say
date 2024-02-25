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

(defonce context (js/AudioContext. {:sampleRate 16000}))

(defn record []
  (js-await [stream (js/navigator.mediaDevices.getUserMedia (clj->js {:audio true}))]
    (js-await [_ (.audioWorklet.addModule context "audio.js")]
      (let [processor (js/AudioWorkletNode. context "processor")]
        (.connect (.createMediaStreamSource context stream) processor)
        (j/assoc-in! processor [:port :onmessage] (fn [message]
                                                    (async/put! chan message.data)))))))
(defn append-float-32-array
  [x y]
  (let [combined (js/Float32Array. (+ (.-length x)
                                      (.-length y)))]
    (.set combined x)
    (.set combined y (.-length x))
    combined))

(def shape
  [2 1 64])

(def tensor
  (ort.Tensor. (js/Float32Array. (apply * shape)) (clj->js shape)))

(defonce state
  (atom {:stream-length 0
         :raw (js/Float32Array.)
         :pause-length 0
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
  (ort.Tensor. (js/BigInt64Array. [(js/BigInt 16000)])))

(defn init []
  (load)
  (record)
  (js-await [session (ort.InferenceSession.create "vad.onnx")]
    (async/go-loop []
      (let [data (async/<! chan)
            state* @state
            combined (append-float-32-array (:raw state*) data)]
        ;; https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
        (if (< (.-length combined) window-size-samples)
          (specter/setval [specter/ATOM :raw] combined state)
          (let [before (js/Float32Array. (take window-size-samples combined))
                input (ort.Tensor. before (clj->js [1 (.-length before)]))
                result (js->clj (->> {:input input
                                      :sr sr}
                                     (merge state*)
                                     clj->js
                                     (.run session)
                                     <p!)
                                :keywordize-keys true)]
            (specter/setval specter/ATOM
                            (merge state*
                                   {:raw (js/Float32Array. (drop window-size-samples combined))
                                    :h (:hn result)
                                    :c (:cn result)}
                                   (if (-> result
                                           :output
                                           .-data
                                           first
                                           (<= 0.5))
                                     {:pause-length (+ (:pause-length state*) (.-length before))}
                                     {:stream-length (+ (:stream-length state*) (.-length before))
                                      :pause-length 0
                                      :vad true}))
                            state)))
        (recur)))))

(def ar 16000)

(def pause-duration 1.5)

(def samples-in-pause (* ar pause-duration))

(def stream-duration 60)

(def samples-in-stream (* ar stream-duration))
