(ns red.device.client.source
  (:require [red.device.client.device :refer [added-device?* add-device get-all-devices]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref PersistentArrayMap)
           (org.joda.time DateTime)))

(defrecord Source [^Ref                clients
                   ^PersistentArrayMap subscribe
                   ^Ref                header-data
                   ^Ref                device->flow
                   ^Ref                client->flow
                   ^Ref                last-data-time
                   ^clojure.lang.Fn    client->dev
                   ^clojure.lang.Fn    client->close
                   ^DateTime           start-time])

(declare get-all-sources)

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
  [{:keys [session-type] :as subscribe}]
  (dosync
   (when (= :realplay session-type)
     (some (fn [{subscribed :subscribe :as source}]
             (when (= subscribe subscribed)
               source))
           (get-all-sources)))))

(defn get-all-sources []
  (dosync
   (reduce (fn [c {sources :sources}] (clojure.set/union c))
           #{} (get-all-devices))))

(defn get-source!
  "获取源,即生成执行程序并建立联系"
  [subscribe]
  (dosync
   (if-let [source (can-cource-multiplex?* subscribe)]
     source
     (create-source! subscribe))))
