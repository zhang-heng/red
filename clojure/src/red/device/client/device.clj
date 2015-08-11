(ns red.device.client.device
  (:require [clojure.tools.logging :as log]
            [red.device.sdk.core :refer [create-exe!]]
            [red.utils :refer [now]])
  (:import [red.device.sdk.core Executor]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.info LoginAccount]
           [clojure.lang Ref PersistentArrayMap]
           [java.util UUID]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]))

(defrecord Device [^UUID         id
                   ^Executor     executor
                   ^LoginAccount account      ;;设备账号
                   ^String       manufacturer ;;厂商
                   ^Ref          sources      ;;媒体请求列表
                   ^Ref          device->flow ;;来自设备的流量统计
                   ^Ref          client->flow ;;来自客户端的流量统计
                   ^DateTime     start-time]
  Sdk$Iface
  (Login [this device-id account]
    (.Login executor device-id account))

  (Logout [this device-id]
    (.Logout executor device-id))

  (StartRealPlay [this device-id source-id info]
    (.StartRealPlay executor device-id source-id info))

  (StopRealPlay [this device-id source-id]
    (.StopRealPlay executor device-id source-id))

  (StartVoiceTalk [this device-id source-id info]
    (.StartVoiceTalk executor device-id source-id info))

  (StopVoiceTalk [this device-id source-id]
    (.StopVoiceTalk executor device-id source-id))

  (SendVoiceData [this device-id source-id data]
    (dosync
     (alter client->flow + (.limit data)))
    (.SendVoiceData executor device-id source-id data))

  (PlayBackByTime [this device-id source-id info]
    (.PlayBackByTime executor device-id source-id info))

  (StopPlayBack [this device-id source-id]
    (.StopPlayBack executor device-id source-id))

  Notify$Iface
  (Lanuched [this _]
    (.Login this id account))

  (Connected [this _]
    (dosync
     (doseq [source (deref sources)]
       (.Connected ^Notify$Iface (val source) _))))

  (Offline [this _]
    (dosync
     (doseq [source (deref sources)]
       (.Offline ^Notify$Iface (val source) _))))

  (MediaStarted [this _ source-id]
    (dosync
     (when-let [source (get (deref sources) source-id)]
       (.MediaStarted ^Notify$Iface source _ source-id))))

  (MediaFinish [this _ source-id]
    (dosync
     (when-let [source (get (deref sources) source-id)]
       (.MediaFinish ^Notify$Iface source _ source-id))))

  (MediaData [this _ source-id {^ByteBuffer payload :payload :as data}]
    (dosync
     (alter device->flow + (.limit payload))
     (when-let [source (get (deref sources) source-id)]
       (.MediaData ^Notify$Iface source _ source-id data))))

  Object
  (toString [_]
    (let [{:keys [addr port user password]} account]
      (str user ":" password "@" addr ":" port
           (map (fn [source] (str (val source))) sources)))))

(defn- creat-device!
  [manufacturer ^LoginAccount account]
  (dosync
   (let [id           (UUID/randomUUID)
         executor     (create-exe! manufacturer)
         device       (Device. id executor account manufacturer (ref {}) (ref 0) (ref 0) (now))]
     (alter (:devices device) assoc id device)
     device)))

(defn get-all-devices
  "获取所有设备数据" []
  (dosync
   (reduce (fn [c {devices :devices}]
             (clojure.set/union c (vals (deref devices))))
           #{} (get-all-executors))))

(defn- added-device?*
  "设备是否已添加"
  [manufacturer* ^LoginAccount account*]
  (dosync
   (some (fn [{:keys [account manufacturer] :as device}]
           (when (and (= manufacturer* manufacturer)
                      (= account*      account))
             device))
         (get-all-devices))))

(defn add-device!
  "添加设备"
  [manufacturer ^LoginAccount account]
  (dosync
   (if-let [device (added-device?* manufacturer account)]
     device
     (creat-device! manufacturer account))))
