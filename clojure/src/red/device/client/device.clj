(ns red.device.client.device
  (:require [red.exe.core :refer [can-exe-multiplex?* create-exe get-all-executors]])
  (:import (clojure.lang Ref)
           (org.joda.time DateTime)))

(defrecord Device [^Ref               sources
                   ^clojure.lang.Fn   notify<-offline       ;;掉线通知
                   ^clojure.lang.Fn   notify<-media-finish  ;;媒体结束通知
                   ^clojure.lang.Fn   notify<-media-data    ;;媒体数据通知
                   ^clojure.lang.Fn   close<-client
                   ^DateTime          start-time])

(defn- mk-device*notify<-offline [device]
  (fn []
    ))
(defn- mk-device*notify<-media-finish [device]
  (fn []))

(defn- mk-device*notify<-media-data [device]
  (fn [media-type byte-buffer]))

(defn add-device
  "添加设备"
  [{:keys [] :as subscribe}]
  (let [device (ref nil)
        exe    (if-let [exe (can-exe-multiplex?* subscribe)]
                 exe
                 (create-exe subscribe))]
    ;; (Device. exe (ref nil) (mk-device*notify<-offline ))
    ))

(defn added-device?*
  "设备是否已添加"
  [{:keys [devices] :as exe} subscribe]
  (some (fn [device]
          (let [{subscribe-b :subscribe {device :device} :source} (deref device)]
            (when (= (select-keys [:addr :port] subscribe)
                     (select-keys [:addr :port] subscribe-b))
              device)))
        (deref devices)))

(defn get-all-devices []
  (dosync
   (reduce (fn [c {devices :devices}] (clojure.set/union c))
           #{} (get-all-executors))))
