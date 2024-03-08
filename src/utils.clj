(ns utils
  (:require [com.rpl.specter :as specter]))

;; This macro is a wrapper around Specter's setval function. The purpose of this macro is to address
;; a specific behavior of Specter's setval, where it may insert nil values into the structure.
;; Instead, this macro ensures that nil values are not created in the structure. If the value to be set
;; is nil, it uses specter/NONE to prevent the insertion of nil.
(defmacro setval
  [apath aval structure]
  `(specter/setval ~apath
                   (if (nil? ~aval)
                     specter/NONE
                     ~aval)
                   ~structure))
