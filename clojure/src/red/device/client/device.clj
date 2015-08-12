(ns red.device.client.device
  (:require [clojure.tools.logging :as log]
            [red.device.sdk.core :refer [create-exe! get-all-executors add-device]]
            [red.utils :refer [now]])
  (:import [red.device.sdk.core Executor ]
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
  (Login [this account _]
    (.Login executor account id))

  (Logout [this _]
    (.Logout executor id))

  (StartRealPlay [this info source-id _]
    (.StartRealPlay executor info source-id id))

  (StopRealPlay [this source-id _]
    (.StopRealPlay executor source-id id))

  (StartVoiceTalk [this info source-id _]
    (.StartVoiceTalk executor info source-id id))

  (StopVoiceTalk [this source-id _]
    (.StopVoiceTalk executor source-id id))

  (SendVoiceData [this {^ByteBuffer payload :payload :as data} source-id _]
    (dosync
     (alter client->flow + (.limit payload)))
    (.SendVoiceData executor data source-id id))

  (PlayBackByTime [this info source-id _]
    (.PlayBackByTime executor info source-id id))

  (StopPlayBack [this source-id _]
    (.StopPlayBack executor source-id id))

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

  (MediaStarted [this source-id _]
    (dosync
     (when-let [source (get (deref sources) source-id)]
       (.MediaStarted ^Notify$Iface source source-id _))))

  (MediaFinish [this source-id _]
    (dosync
     (when-let [source (get (deref sources) source-id)]
       (.MediaFinish ^Notify$Iface source _ source-id))))

  (MediaData [this {^ByteBuffer payload :payload :as data} source-id _]
    (dosync
     (alter device->flow + (.limit payload))
     (when-let [source (get (deref sources) source-id)]
       (.MediaData ^Notify$Iface source data source-id _))))

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
     (add-device executor device)
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

(defn add-source
  [{:keys [sources] :as device}
   {:keys [id]      :as source}]
  (alter sources assoc id source))

(defn add-device!
  "添加设备"
  [manufacturer ^LoginAccount account]
  (dosync
   (if-let [device (added-device?* manufacturer account)]
     device
     (creat-device! manufacturer account))))
