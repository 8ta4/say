(ns main
  (:require [electron]
            [process]
            [shared :refer [channel]]))

(def shortcut
  "Command+;")

(def background
  (= "--background" (last process/argv)))

(defn initialize []
  (when background
    (electron/app.dock.hide))
  (let [win (electron/BrowserWindow. (clj->js {:width 800
                                               :height 600
                                               :webPreferences {:nodeIntegration true
;; https://stackoverflow.com/questions/44391448/electron-require-is-not-defined#:~:text=without%20the%20line%20contextisolation%3A%20false%2C%20you%20won't%20be%20able%20to%20access%20require
                                                                :contextIsolation false}
                                               :show (not background)}))]
    (.loadFile win "public/index.html")
    (electron/globalShortcut.register shortcut (fn []
                                                 (js/console.log "Global shortcut" shortcut "pressed.")
                                                 (.webContents.send win channel)))
    (.on win "close" (fn [event]
                       (js/console.log "Window close event triggered.")
                       (.preventDefault event)
                       (.hide win)
                       (electron/app.dock.hide)))
    (electron/app.on "activate" (fn []
                                  (js/console.log "App activated, showing window.")
                                  (.show win)
                                  (electron/app.dock.show))))
  (electron/app.relaunch)
  (electron/powerSaveBlocker.start "prevent-app-suspension"))

(defn main []
  (js/console.log "App is ready, initializing...")
  (.then (electron/app.whenReady) initialize))
