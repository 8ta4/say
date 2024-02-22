(ns renderer
  (:require [reagent.dom.client :as client]))

(defn init []
  (client/render (client/create-root (js/document.getElementById "app"))
                 [:div "Hello, Renderer!"])
  (println "Hello, Renderer!"))