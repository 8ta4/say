(ns clj.core
  (:gen-class)
  (:require
   [libpython-clj2.python :as py]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(py/initialize! :python-executable "../.venv/bin/python")