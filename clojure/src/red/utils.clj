(ns red.utils
  (:require [clj-time.core :as time]
            [clj-time.coerce :refer [to-long]]
            [clj-time.format :refer [parse unparse formatters with-zone]]))

(defn now []
  (time/to-time-zone
   (time/now)
   (time/time-zone-for-id "Asia/Shanghai")))

(defn pass-mill [t]
  (- (to-long (now)) (to-long t)))

(defn zh-cn-time-str ^String [dt]
  (-> (with-zone (:date-time-no-ms formatters) (time/time-zone-for-id "Asia/Shanghai"))
      (unparse dt)))
