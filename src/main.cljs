(ns main
  (:require [electron :refer [BrowserWindow]]))

(defn main []
  (println "Hello, Electron!"))

(defn create-window
  []
  (let [win (BrowserWindow. #js {:width 800, :height 600})]
    (.loadFile win "public/index.html")))