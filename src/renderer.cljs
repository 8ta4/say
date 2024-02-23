(ns renderer
  (:require ["@mui/material/TextField" :default TextField]
            [com.rpl.specter :as specter]
            [reagent.core :as reagent]
            [reagent.dom.client :as client]))

(def os
  (js/require "os"))

(def path
  (js/require "path"))

(defonce root
  ;; Using defonce to ensure the root is only created once. This prevents warnings about
  ;; calling ReactDOMClient.createRoot() on a container that has already been passed to
  ;; createRoot() before during hot reloads or re-evaluations of the code.
  (client/create-root (js/document.getElementById "app")))

(defonce secrets (reagent/atom {:key ""}))

(defn init []
  (client/render root [:> TextField {:label "Deepgram API Key"
                                     :type "password"
                                     :on-change (fn [event]
                                                  (specter/setval [specter/ATOM :key] event.target.value secrets))}])
  (println "Hello, Renderer!"))

(def config-path (path.join (os.homedir) ".config/say/secrets.yaml"))