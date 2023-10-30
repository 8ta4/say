(ns clj.core
  (:gen-class)
  (:require [cheshire.core :refer [parse-string]]
            [clj-http.client :as client]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]
            [com.rpl.specter :refer [setval AFTER-ELEM]]
            [libpython-clj2.codegen :as codegen]
            [libpython-clj2.python :as py]
            [mount.core :as mount :refer [defstate]]
            [ring.adapter.jetty :refer [run-jetty]]
            [tick.core :as t])
  (:import [java.nio.file Files StandardCopyOption]))

(py/initialize! :python-executable "../.venv/bin/python")

(codegen/write-namespace! "builtins" {:symbol-name-remaps {"AssertionError" "PyAssertionError"
                                                           "Exception" "PyException"}})
(codegen/write-namespace! "numpy")
(codegen/write-namespace! "pyaudio")
(codegen/write-namespace! "subprocess")
(codegen/write-namespace! "torch" {:symbol-name-remaps {"Callable" "PyCallable"}})

(require '[python.builtins :as python])
(require '[python.numpy :as np])
(require '[python.pyaudio :as pyaudio])
(require '[python.subprocess :as sp])
(require '[python.torch :as torch])

; https://github.com/snakers4/silero-vad/blob/cb92cdd1e33cc1eb9c4ae3626bf3cd60fc660976/utils_vad.py#L207
(def chunk-size 1536)

; 16 bits per sample
(def sample-format pyaudio/paInt16)

(def channels 1)

; https://github.com/snakers4/silero-vad/blob/cb92cdd1e33cc1eb9c4ae3626bf3cd60fc660976/utils_vad.py#L207
(def fs 16000)

(def audio-filepath (str (System/getProperty "java.io.tmpdir") "/output.mp3"))

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
        l (sp/Popen ["lame" "-" "-r" "-m" "m" "-s" "16" audio-filepath] :stdin sp/PIPE)]
    (py/call-attr-kw l "communicate" [] {:input raw-pcm})))

(def manual-trigger (atom false))

(def pause-duration-limit 1.5)

(def audio-duration-limit 60)

(defn calculate-duration [frames]
  (/ (* (count frames) chunk-size) fs))

(def audio-channel (async/chan))

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
        (async/>!! audio-channel updated-main-buffer)
        (recur [] updated-temp-buffer ##Inf))
      (recur updated-main-buffer updated-temp-buffer updated-last-voice-activity))))

(defn format-transcription
  "Format the parsed response into a string of sentences."
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
       (map :text)
       (str/join "\n")
       (str "\n\n")))

(defn get-headers [api-key]
  {"Authorization" (str "Token " api-key)})

(def url "https://api.deepgram.com/v1/listen?smart_format=true&model=nova-2-ea&language=en-US")

(defn get-parsed-response
  "Make a POST request to the Deepgram API and return the parsed response body."
  [api-key]
  (-> (client/post url {:headers (get-headers api-key) :body (io/file audio-filepath)})
      :body
      (parse-string true)))

(defn get-transcript-path []
  (str (System/getProperty "user.home") "/.local/share/say/" (t/format (t/formatter "yyyy/MM/dd") (t/date)) ".txt"))

(defn atomic-rename [source-path target-path]
  (Files/move (.toPath (io/file source-path))
              (.toPath (io/file target-path))
              (into-array StandardCopyOption [StandardCopyOption/ATOMIC_MOVE])))

(defn transcribe
  "Make a POST request to the Deepgram API and write the transcribed text to a file."
  [transcript-path api-key]
  (io/make-parents transcript-path)
  (spit transcript-path (format-transcription (get-parsed-response api-key)) :append true))

(defn open-in-vscode [transcript-path]
  (let [line-count (with-open [rdr (io/reader transcript-path)]
                     (count (line-seq rdr)))]
    (sh "code" "-g" (str transcript-path ":" line-count))))

(defn process-audio []
  (while true
    (save-audio (async/<!! audio-channel))
    (let [transcript-path (get-transcript-path)]
      (transcribe transcript-path (mount/args))
      (open-in-vscode transcript-path))))

(defn handler [_]
  (reset! manual-trigger true)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Triggered"})

(defstate server
  :start (run-jetty handler {:port 8080 :join? false})
  :stop (.stop server))

(defstate audio-processing
  :start (future (process-audio))
  :stop (future-cancel audio-processing))

(defstate recording
  :start (future (continuously-record [] [] ##Inf))
  :stop (future-cancel recording))

(def -main mount/start-with-args)