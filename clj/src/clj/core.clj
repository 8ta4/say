(ns clj.core
  (:gen-class)
  (:require
   [libpython-clj2.python :as py]
   [libpython-clj2.require :refer [require-python]]))

(py/initialize! :python-executable "../.venv/bin/python")

(require-python '[builtins :as python])
(require-python 'pyaudio)
(require-python 'wave)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

; Record in chunks of 1024 samples
; TODO: Evaluate if chunk-size and fs values are optimal for the current use case
(def chunk-size 1024)

; 16 bits per sample
(def sample-format pyaudio/paInt16)

(def channels 1)

; Record at 44100 samples per second
(def fs 44100)

; TODO: Ensure the recording continues indefinitely
(def seconds 3)

; TODO: Revisit the decision to use .wav format
(def filename "output.wav")

(def p (pyaudio/PyAudio))

(def stream (py/call-attr-kw p "open" [] {:format sample-format
                                          :channels channels
                                          :rate fs
                                          :frames_per_buffer chunk-size
                                          :input true}))

(defn read-chunk []
  (py/call-attr stream "read" chunk-size))

(def frames (vec (take (int (* (/ fs chunk-size) seconds)) (repeatedly read-chunk))))

; Stop and close the stream
(py/call-attr stream "stop_stream")
(py/call-attr stream "close")

; Terminate the PortAudio interface
(py/call-attr p "terminate")

; Save the recorded data as a WAV file
(def wf (wave/open filename "wb"))
(py/call-attr wf "setnchannels" channels)
(py/call-attr wf "setsampwidth" (py/call-attr p "get_sample_size" sample-format))
(py/call-attr wf "setframerate" fs)

(def empty-bytes (python/bytes "" "utf-8"))

(py/call-attr wf "writeframes" (py/call-attr empty-bytes "join" frames))
(py/call-attr wf "close")