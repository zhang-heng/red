(ns red.device.device
  (:require [clojure.tools.logging :as log]
            [red.device.exe :refer [create-exe! get-all-executors add-device remove-device]]
            [red.device.operate :refer :all]
            [red.utils :refer [now]])
  (:import [red.device.exe Executor]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.info LoginAccount]
           [clojure.lang Ref PersistentArrayMap Atom]
           [java.util UUID]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]))

(defprotocol IDevice
  (get-device-id [this])

  (add-source [this source id])
  (remove-source [this id])

  (get-device-status [this])
  (set-gateway [this gw])
  (remove-gateway [this]))

(deftype Device [^String       id
                 ^Sdk$Iface    executor
                 ^String       manufacturer ;;厂商
                 ^LoginAccount account      ;;设备账号
                 ^Ref          sources ;;媒体请求列表 (ref {id source ...})
                 ^Ref          gateway ;;网关列表 (ref  gateway)
                 ^Atom         device->flow ;;来自设备的流量统计
                 ^Atom         client->flow ;;来自客户端的流量统计
                 ^Ref          status ;; :connecting :online :offline
                 ^DateTime     start-time]
  IDevice
  (get-device-status [this]
    @status)

  (get-device-id [this]
    id)

  (add-source [this source id]
    (dosync
     (alter sources assoc id source)))

  (remove-source [this id]
    (dosync
     (when (and (empty? (alter sources dissoc id))
                (nil? @gateway))
       (close this))))

  (set-gateway [this gw]
    (dosync
     (ref-set gateway gw)))

  (remove-gateway [this]
    (dosync
     (ref-set gateway nil)
     (when (and (empty? @sources))
       (close this))))

  IOperate
  (can-multiplex? [this args]
    (let [[manufacturer* account*] args]
      (and (= manufacturer* manufacturer)
           (= account*      account))))

  (close [this]
    (dosync
     (log/info "close device")
     ;;从进程层将本对象删除
     (remove-device executor id)
     ;;通知所有子层关闭
     (doseq [pgateway (deref gateway)]
       (close (val pgateway)))
     (doseq [psource (deref sources)]
       (close (val psource)))
     ;;请求断开此设备
     (.Logout executor id)))

  Sdk$Iface
  (Login [this _ _]
    (when (= :offline @status)
      (dosync (ref-set status :connecting))
      (.Login executor account id)))

  (Logout [this _]
    (when (= :online @status)
      (.Logout executor id)))

  (StartMedia [this info source-id _]
    (when (= :online @status)
      (.StartMedia executor info source-id id)))

  (StopMedia [this source-id _]
    (.StopMedia executor source-id id))

  (SendMediaData [this data source-id _]
    (dosync
     (swap! client->flow + (.limit data)))
    (.SendMediaData executor data source-id id))

  Notify$Iface
  (Lanuched [this _]
    ;;进程启动成功,请求登陆
    (.Login this account id))

  (Connected [this _]
    (log/infof "device connected: %s" id)
    (dosync
     (ref-set status :online))
    ;;告知所有子对象,可以做进一步请求
    (doseq [psource (deref sources)]
      (.Connected ^Notify$Iface (val psource) _))
    (when @gateway
      (.Connected ^Notify$Iface @gateway _)))

  (Offline [this _]
    (dosync
     (ref-set status :offline)
     ;;告知所有子对象,由子对象决定下一步操作
     (doseq [psource (deref sources)]
       (.Offline ^Notify$Iface (val psource) _))
     (when @gateway
       (.Offline ^Notify$Iface @gateway _))))

  (MediaStarted [this source-id _]
    "当无对应，则应该关闭媒体 todo..."
    (dosync
     (when-let [source (get (deref sources) source-id)]
       (.MediaStarted ^Notify$Iface source source-id _))))

  (MediaFinish [this source-id _]
    (dosync
     (when-let [source (get (deref sources) source-id)]
       (.MediaFinish ^Notify$Iface source source-id _ ))))

  (MediaData [this data source-id _]
    (let [{:keys [^bytes payload]} (bean data)]
      (swap! device->flow + (alength payload)))
    (when-let [source (get (deref sources) source-id)]
      (.MediaData ^Notify$Iface source data source-id _)))

  clojure.lang.IDeref
  (deref [_] {:sources @sources :gateway @gateway})

  Object
  (toString [_]
    (let [{:keys [addr port]} (bean account)
          sources (->> @sources vals (map str))]
      (format "__device: %s:%d \n%s" addr port
              (->> sources
                   (clojure.string/join ",\n"))))))

(defn- creat-device!
  [manufacturer ^LoginAccount account]
  (dosync
   (let [{:keys [addr port]} (bean account)]
     (log/infof "create device: %s:%d" addr port))

   (let [id           (str (UUID/randomUUID))
         executor     (create-exe! manufacturer)
         device       (Device. id executor manufacturer account (ref {}) (ref nil) (atom 0) (atom 0) (ref :offline) (now))]
     (add-device executor device id)
     device)))

(defn get-all-devices
  "获取所有设备数据" []
  (dosync
   (reduce (fn [c pexecutor]
             (->> pexecutor val deref (conj c)))
           {} (get-all-executors))))

(defn- added-device?*
  "设备是否已添加"
  [manufacturer account]
  (dosync
   (some (fn [pdevice]
           (let [device (val pdevice)]
             (when (can-multiplex+? device manufacturer account)
               device)))
         (get-all-devices))))

(defn add-device!
  "添加设备"
  [manufacturer ^LoginAccount account]
  (dosync
   (if-let [device (added-device?* manufacturer account)]
     device
     (creat-device! manufacturer account))))
