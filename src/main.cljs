(ns main
  (:require [electron :refer [app BrowserWindow globalShortcut]]))

(defn create-window
  []
  (let [win (BrowserWindow. (clj->js {:width 800
                                      :height 600
                                      :webPreferences {:nodeIntegration true

                                                      ;; https://stackoverflow.com/questions/44391448/electron-require-is-not-defined#:~:text=without%20the%20line%20contextisolation%3A%20false%2C%20you%20won't%20be%20able%20to%20access%20require
                                                       :contextIsolation false}}))]
    (.loadFile win "public/index.html")))

(defn register-shortcut []
  (.register globalShortcut "Command+;" (fn []
                                          (js/console.log "Command+; is pressed"))))

(defn main []
  (println "Hello, Electron!")
  (.then (.whenReady app) (fn []
                            (create-window)
                            (register-shortcut))))