(ns renderer
  (:require [reagent.dom.client :as client]))

(defonce root
  ;; Using defonce to ensure the root is only created once. This prevents warnings about
  ;; calling ReactDOMClient.createRoot() on a container that has already been passed to
  ;; createRoot() before during hot reloads or re-evaluations of the code.
  (client/create-root (js/document.getElementById "app")))

(defn init []
  (client/render root [:div "Hello, Renderer!"])
  (println "Hello, Renderer!"))