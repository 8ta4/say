(ns main
  (:require [electron]
            [shared :refer [channel]]))

(def shortcut
  "Command+;")

(defn initialize-window []
  (let [win (electron/BrowserWindow. (clj->js {:width 800
                                               :height 600
                                               :webPreferences {:nodeIntegration true
;; https://stackoverflow.com/questions/44391448/electron-require-is-not-defined#:~:text=without%20the%20line%20contextisolation%3A%20false%2C%20you%20won't%20be%20able%20to%20access%20require
                                                                :contextIsolation false}}))]
    (.loadFile win "public/index.html")
    (electron/globalShortcut.register  shortcut (fn []
                                                  (js/console.log "Global shortcut" shortcut "pressed.")
                                                  (.webContents.send win channel)))
    (electron/powerSaveBlocker.start "prevent-app-suspension")))

(defn main []
  (js/console.log "Hello, Electron!")
  (.then (electron/app.whenReady) initialize-window))
