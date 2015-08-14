(ns red.device.client.device
  (:require [clojure.tools.logging :as log]
            [red.device.client.sdk.core :refer [create-exe! get-all-executors]]
            [red.device.client.operate :refer :all]
            [red.utils :refer [now]])
  (:import [red.device.client.sdk.core Executor]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.info LoginAccount]
           [clojure.lang Ref PersistentArrayMap]
           [java.util UUID]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]))

(defprotocol IDevice
  (add-source [this source id]))

(deftype Device [^String       id
                 ^Executor     executor
                 ^String       manufacturer ;;厂商
                 ^LoginAccount account      ;;设备账号
                 ^Ref          sources ;;媒体请求列表 (ref {id source ...})
                 ^Ref          device->flow ;;来自设备的流量统计
                 ^Ref          client->flow ;;来自客户端的流量统计
                 ^DateTime     start-time]
  IDevice
  (add-source [this source id]
    (dosync
     (alter sources assoc id source)))

  IOperate
  (can-multiplex? [this args]
    (let [[manufacturer* account*] args]
      (and (= manufacturer* manufacturer)
           (= account*      account))))

  (close [this]
    (dosync
     (log/info "close device")
     ;;请求断开此设备
     (.Logout executor id)
     ;;从进程层将本对象删除
     (alter @executor dissoc id)
     ;;通知所有子层关闭 todo... 网关模式还要增加处理
     (doseq [psource (deref sources)]
       (let [source-id (key psource)
             source    (val psource)]
         (alter sources dissoc source-id)
         (.Offline ^Notify$Iface source nil)))))

  Sdk$Iface
  (Login [this _ _]
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
    ;;进程启动成功,请求登陆
    (.Login this account id))

  (Connected [this _]
    (dosync
     ;;告知所有媒体源,可以做媒体请求
     (doseq [source (deref sources)]
       (.Connected ^Notify$Iface source _))))

  (Offline [this _]
    (dosync
     (doseq [psource (deref sources)]
       (let [source-id (key psource)
             source (val psource)]
         (alter sources dissoc source-id)
         (.Offline ^Notify$Iface source _)))))

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

  clojure.lang.IDeref
  (deref [_] sources)

  Object
  (toString [_]
    (let [{:keys [addr port]} (bean account)]
      (format "__device: %s:%d \n%s"
              addr port
              (->> @sources
                   vals
                   (map #(str %))
                   (clojure.string/join ",\n"))))))

(defn- creat-device!
  [manufacturer ^LoginAccount account]
  (dosync
   (let [{:keys [addr port]} (bean account)]
     (log/infof "create device: %s:%d" addr port))

   (let [id           (str (UUID/randomUUID))
         executor     (create-exe! manufacturer)
         device       (Device. id executor manufacturer account (ref {}) (ref 0) (ref 0) (now))]
     (.add-device ^Executor executor device id)
     device)))

(defn get-all-devices
  "获取所有设备数据" []
  (dosync
   (reduce (fn [c executor]
             (clojure.set/union c (vals (deref (deref executor)))))
           #{} (get-all-executors))))

(defn- added-device?*
  "设备是否已添加"
  [manufacturer account]
  (dosync
   (some (fn [device]
           (when (can-multiplex+? device manufacturer account)
             device))
         (get-all-devices))))

(defn add-device!
  "添加设备"
  [manufacturer ^LoginAccount account]
  (dosync
   (if-let [device (added-device?* manufacturer account)]
     device
     (creat-device! manufacturer account))))
