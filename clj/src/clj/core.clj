(ns clj.core
  (:gen-class)
  (:require
   [libpython-clj2.python :as py]
   [libpython-clj2.require :refer [require-python]]))

(py/initialize! :python-executable "../.venv/bin/python")

(require-python '[builtins :as python])
(require-python '[numpy :as np])
(require-python 'pyaudio)
(require-python 'spacy)
(require-python '[subprocess :as sp])
(require-python 'torch)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

; TODO: Evaluate if chunk-size and fs values are optimal for the current use case
(def chunk-size 1536)

; 16 bits per sample
(def sample-format pyaudio/paInt16)

(def channels 1)

(def fs 16000)

; TODO: Ensure the recording continues indefinitely
(def seconds 3)

(def filename "output.mp3")

(def p (pyaudio/PyAudio))

(def stream (py/call-attr-kw p "open" [] {:format sample-format
                                          :channels channels
                                          :rate fs
                                          :frames_per_buffer chunk-size
                                          :input true}))

(defn read-chunk []
  (py/call-attr stream "read" chunk-size))

(def frames (vec (take (int (* (/ fs chunk-size) seconds)) (repeatedly read-chunk))))

(def model (first (py/call-attr torch/hub "load" "snakers4/silero-vad" "silero_vad")))

; https://github.com/snakers4/silero-vad/blob/563106ef8cfac329c8be5f9c5051cd365195aff9/examples/pyaudio-streaming/pyaudio-streaming-examples.ipynb#L117-L123
(defn int2float [sound]
  (let [abs-max (py/call-attr sound "max")
        sound (py/call-attr sound "astype" "float32")]
    (if (> abs-max 0)
      (py/call-attr sound "__mul__" (/ 1 32768)))
    (py/call-attr sound "squeeze")))

(defn vad? [audio-chunk]
  (let [audio-int16 (np/frombuffer audio-chunk np/int16)
        audio-float32 (int2float audio-int16)
        confidence (model (torch/from_numpy audio-float32) 16000)]
    (<= 0.5 (py/call-attr confidence "item"))))

; Save the recorded data
(def empty-bytes (python/bytes "" "utf-8"))

; https://stackoverflow.com/a/63794529
(def raw-pcm (py/call-attr empty-bytes "join" frames))
(def l (sp/Popen ["lame" "-" "-r" "-m" "m" "-s" "16" filename] :stdin sp/PIPE))
(py/call-attr-kw l "communicate" [] {:input raw-pcm})

(def nlp (spacy/load "en_core_web_sm"))

(defn segment-sentences [input_text]
  (let [doc (nlp input_text)
        sentences (py/get-attr doc "sents")]
    (map #(py/get-attr % "text") sentences)))
