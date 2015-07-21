(ns red.device.client.source
  (:require [red.device.client.device :refer [added-device?* add-device get-all-devices]])
  (:import (clojure.lang Ref PersistentArrayMap)
           (org.joda.time DateTime)))

(defrecord Source [^Ref                clients
                   ^PersistentArrayMap subscribe
                   ^Ref                header-data
                   ^Ref                flow<-device
                   ^Ref                flow->device
                   ^Ref                last-data-time
                   ^clojure.lang.Fn    close<-client
                   ^DateTime           start-time])

(defn- create-source!
  "新建媒体源"
  [subscribe]
  (let [device (if-let [device (added-device?* subscribe)]
                 device
                 (add-device [subscribe]))]
    ;; (Source. device (ref 0) subscribe (ref 0) (ref 0) (ref (now)) (now))
    ))

(defn- can-cource-multiplex?*
  "源能否复用"
  [sources {:keys [session-type] :as subscribe}]
  (dosync
   (and (= :realplay session-type)
        (some (fn [source]
                (let [{subscribed :subscribe} (deref source)]
                  (when (= subscribe subscribed)
                    source)))
              (deref sources)))))

(defn get-source!
  "获取源,即生成执行程序并建立联系"
  [subscribe]
  (dosync
   (if-let [source (can-cource-multiplex?* subscribe)]
     source
     (create-source! subscribe))))

(defn get-all-sources []
  (dosync
   (let [{:keys [sources]} (get-all-devices)]
     (deref sources))))
