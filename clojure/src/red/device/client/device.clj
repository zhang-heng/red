(ns red.device.client.device
  (:require [red.exe.core :refer [create-exe! get-all-executors
                                  login logout client->device client->close open-source]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref)
           (java.util UUID)
           (java.nio ByteBuffer)
           (org.joda.time DateTime)))

(defrecord Device [^Ref               sources
                   ^Ref               device-id
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
  [executor device-id client->flow]
  (fn [source-id buffer]
    (dosync
     (alter client->flow + (.limit buffer))
     (client->device executor device-id source-id buffer))))

(defn- mk-client->close
  [executor device-id]
  (fn [source-id]
    (dosync
     (client->close executor device-id source-id))))

(defn- mk-device->connected
  [devices executor]
  (fn [device-id]
    (dosync
     (when-let [{:keys [sources]} (get (deref devices) device-id)]
       (doseq [{:keys [source-id]} sources]
         (open-source executor device-id source-id))))))

(defn- mk-device->offline
  [devices]
  (fn [device-id]
    (dosync
     (when-let [{:keys [sources]} (get (deref devices) device-id)]
       (doseq [{:keys [device->close]} sources]
         (device->close))))))

(defn- mk-device->media-finish
  [devices]
  (fn [device-id source-id]
    (dosync
     (when-let [{:keys [sources]} (get (deref devices) device-id)]
       (when-let [{:keys [device->close device->client]}
                  (get (deref sources) source-id)]
         (device->client (ByteBuffer/allocate 0))
         (device->close))))))

(defn- mk-device->media-data
  [devices]
  (fn [device-id source-id buffer]
    (dosync
     (when-let [{:keys [sources]} (get (deref devices) device-id)]
       (when-let [{:keys [device->close device->client]}
                  (get (deref sources) source-id)]
         (device->client buffer))))))

(defn- mk-handler
  []
  (fn []))

(defn- creat-device! [device-info]
  (dosync
   (let [{:keys [devices] :as executor} (create-exe! device-info)
         sources (ref #{})
         device-id (UUID/randomUUID)
         client->flow (ref 0)
         device->flow (ref 0)
         device  (Device. sources device-id
                          (mk-device->connected sources executor)
                          (mk-device->offline sources)
                          (mk-device->media-finish sources)
                          (mk-device->media-data sources)
                          (mk-client->close executor executor device-id)
                          (mk-client->device executor executor device-id client->flow)
                          client->flow device->flow (now))]
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
