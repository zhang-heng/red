(ns red.device.client.device
  (:require [red.exe.core :refer [create-exe! get-all-executors login logout]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref)
           (org.joda.time DateTime)))

(defrecord Device [^Ref               sources
                   ^Ref               gateway
                   ^clojure.lang.Fn   handler
                   ^clojure.lang.Fn   device->connected     ;;登陆成功
                   ^clojure.lang.Fn   device->offline       ;;掉线通知
                   ^clojure.lang.Fn   device->media-finish  ;;媒体结束通知
                   ^clojure.lang.Fn   device->media-data    ;;媒体数据通知
                   ^clojure.lang.Fn   client->close
                   ^clojure.lang.Fn   client->device
                   ^Ref               client->flow
                   ^Ref               device->flow
                   ^DateTime          start-time])

(declare get-all-devices)

(defn- mk-client->device
  [sources client->flow]
  (fn [source-id buffer]
    (dosync
     (alter client->flow + (.limit buffer))
     (when-let [{:keys [client->device] :as source} (get (deref sources) source-id)]
       (client->device buffer)))))

(defn- mk-client->close
  [sources]
  (fn [source-id]
    (dosync
     (when-let [{:keys [client->close] :as source} (get (deref sources) source-id)]
       (client->close)))))

(defn- mk-device->connected
  [sources source-id]
  (fn [device-id]
    (when-let [{:keys [subscribe]} (get (deref sources) source-id)]
      (login device-id subscribe))))

(defn- mk-device->offline
  [sources source-id]
  (fn [device-id]
    (when-let [source (get (deref sources) source-id)]
      (login device-id))))

(defn- mk-device->media-finish
  [sources]
  (fn [source-id]))

(defn- mk-device->media-data
  [sources]
  (fn [source-id buffer]))

(defn- mk-handler
  []
  (fn []))

(defn- creat-device! [device-info]
  (dosync
   (let [{:keys [devices] :as executor} (create-exe! device-info)
         sources (ref #{})
         gateway (ref #{})
         client->flow (ref 0)
         device->flow (ref 0)
         device  (Device. sources gateway
                          (mk-handler)
                          (mk-device->connected sources)
                          (mk-device->offline sources)
                          (mk-device->media-finish sources)
                          (mk-device->media-data sources)
                          (mk-client->close executor)
                          (mk-client->device executor)
                          client->flow
                          device->flow
                          (now))]
     (login device-info)
     (alter devices conj device))))

(defn- added-device?*
  "设备是否已添加"
  [device-info]
  (some (fn [{info :subscribe :as device}]
          (when (= device-info info)
            device))
        (get-all-devices)))

(defn get-all-devices
  "获取所有设备数据"
  []
  (dosync
   (reduce (fn [c {devices :devices}] (clojure.set/union c))
           #{} (get-all-executors))))

(defn add-device!
  "添加设备"
  [subscribe]
  (dosync
   (let [device-info (select-keys [:addr :port :password :user] subscribe)]
     (if-let [source (added-device?* device-info)]
       source
       (creat-device! subscribe)))))
