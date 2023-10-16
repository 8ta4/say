(ns clj.core
  (:gen-class)
  (:require
   [libpython-clj2.python :as py]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(py/initialize! :python-executable "../.venv/bin/python")

(def pyaudio (py/import-module "pyaudio"))
(def wave (py/import-module "wave"))

; Record in chunks of 1024 samples
(def chunk 1024)

; 16 bits per sample
(def sample-format (py/get-attr pyaudio "paInt16"))

(def channels 1)
; Record at 44100 samples per second
(def fs 44100)

(def seconds 3)

(def filename "output.wav")

(def p (py/call-attr pyaudio "PyAudio"))
