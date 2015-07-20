(ns red.device.active.realplay
  (:require [red.device.active.core :refer [open-session*]])
  (:import [java.util UUID Date]
           [clojure.lang Ref Keyword IPersistentSet]))

(def sources #{})
(def clients #{})



(defmethod open-session* :realplay [subscribe write-handle close-handle]
  (fn [] (prn "disconnect")))


(type "")
