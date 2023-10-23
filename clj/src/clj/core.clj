(ns clj.core
  (:gen-class)
  (:require [cheshire.core :refer [parse-string]]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [com.rpl.specter :refer [setval AFTER-ELEM]]
            [libpython-clj2.python :as py]
            [libpython-clj2.require :refer [require-python]]
            [ring.adapter.jetty :refer [run-jetty]]))

(py/initialize! :python-executable "../.venv/bin/python")

(require-python '[builtins :as python])
(require-python '[numpy :as np])
(require-python 'pyaudio)
(require-python '[subprocess :as sp])
(require-python 'torch)

; https://github.com/snakers4/silero-vad/blob/cb92cdd1e33cc1eb9c4ae3626bf3cd60fc660976/utils_vad.py#L207
(def chunk-size 1536)

; 16 bits per sample
(def sample-format pyaudio/paInt16)

(def channels 1)

; https://github.com/snakers4/silero-vad/blob/cb92cdd1e33cc1eb9c4ae3626bf3cd60fc660976/utils_vad.py#L207
(def fs 16000)

(def filename "output.mp3")

(def p (pyaudio/PyAudio))

(def stream (py/call-attr-kw p "open" [] {:format sample-format
                                          :channels channels
                                          :rate fs
                                          :frames_per_buffer chunk-size
                                          :input true}))

(defn read-chunk []
  (py/call-attr stream "read" chunk-size))

(def model (first (py/call-attr torch/hub "load" "snakers4/silero-vad" "silero_vad")))

; https://github.com/snakers4/silero-vad/blob/cb92cdd1e33cc1eb9c4ae3626bf3cd60fc660976/examples/pyaudio-streaming/pyaudio-streaming-examples.ipynb?short_path=da46792#L117-L123
(defn int2float [sound]
  (let [abs-max (py/call-attr sound "max")
        sound (py/call-attr sound "astype" "float32")]
    (if (> abs-max 0)
      (py/call-attr sound "__mul__" (/ 1 32768)))
    (py/call-attr sound "squeeze")))

(defn voice-activity? [audio-chunk]
  (let [audio-int16 (np/frombuffer audio-chunk np/int16)
        audio-float32 (int2float audio-int16)
        confidence (model (torch/from_numpy audio-float32) fs)]
    (<= 0.5 (py/call-attr confidence "item"))))

(def empty-bytes (python/bytes "" "utf-8"))

(defn save-audio [frames]
  ; https://stackoverflow.com/a/63794529
  (let [raw-pcm (py/call-attr empty-bytes "join" frames)
        l (sp/Popen ["lame" "-" "-r" "-m" "m" "-s" "16" filename] :stdin sp/PIPE)]
    (py/call-attr-kw l "communicate" [] {:input raw-pcm})))

(def manual-trigger (atom false))

(def pause-duration-limit 1.5)

(def audio-duration-limit 60)

(defn calculate-duration [frames]
  (/ (* (count frames) chunk-size) fs))

(defn continuously-record [main-buffer temp-buffer last-voice-activity]
  (let [audio-chunk (read-chunk)
        updated-last-voice-activity (if (voice-activity? audio-chunk)
                                      0
                                      (+ last-voice-activity (/ chunk-size fs)))
        temp-buffer-with-new-chunk (setval AFTER-ELEM audio-chunk temp-buffer)
        temp-buffer-without-old-chunks (if (< pause-duration-limit (calculate-duration temp-buffer-with-new-chunk))
                                         (rest temp-buffer-with-new-chunk)
                                         temp-buffer-with-new-chunk)
        updated-main-buffer (if (<= updated-last-voice-activity pause-duration-limit)
                              (concat main-buffer temp-buffer-without-old-chunks)
                              main-buffer)
        updated-temp-buffer (if (<= updated-last-voice-activity pause-duration-limit)
                              []
                              temp-buffer-without-old-chunks)]
    (if (and (not-empty updated-main-buffer)
             (or @manual-trigger
                 (and (< audio-duration-limit (calculate-duration updated-main-buffer))
                      (< pause-duration-limit updated-last-voice-activity))))
      (do
        (reset! manual-trigger false)
        (save-audio updated-main-buffer)
        (recur [] updated-temp-buffer ##Inf))
      (recur updated-main-buffer updated-temp-buffer updated-last-voice-activity))))

(defn extract-sentences
  "Extract the sentences from the parsed response."
  [parsed-response]
  (->> parsed-response
       :results
       :channels
       first
       :alternatives
       first
       :paragraphs
       :paragraphs
       (mapcat :sentences)
       (map :text)))

(defn transcribe
  "Make a POST request to the Deepgram API and write the transcribed text to a file."
  [api-key]
  (let [url "https://api.deepgram.com/v1/listen?smart_format=true&model=nova&language=en-US"
        headers {"Authorization" (str "Token " api-key)}
        body (io/file filename)]
    (->> (parse-string (:body (client/post url {:headers headers :body body})) true)
         extract-sentences
         (str/join "\n")
         (spit "output.txt"))))

(defn handler [_]
  (reset! manual-trigger true)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Triggered"})

(defn -main [api-key & args]
  (run-jetty handler {:port 8080 :join? false})
  (continuously-record [] [] ##Inf))