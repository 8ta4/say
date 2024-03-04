(ns utils
  (:require [com.rpl.specter :as specter]))

(defmacro setval
  [apath aval structure]
  `(specter/setval ~apath
                   (if (nil? ~aval)
                     specter/NONE
                     ~aval)
                   ~structure))
