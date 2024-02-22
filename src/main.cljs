(ns main
  (:require [electron :refer [app BrowserWindow]]))

(defn create-window
  []
  (let [win (BrowserWindow. #js {:width 800, :height 600})]
    (.loadFile win "public/index.html")))

(defn main []
  (println "Hello, Electron!")
  (.then (.whenReady app) (fn [] (create-window))))