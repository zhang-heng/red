(ns red.device.source
  (:require [clojure.tools.logging :as log]
            [red.device.device :refer [add-device! get-all-devices add-source remove-source]]
            [red.device.operate :refer :all]
            [red.utils :refer [now Timeinfo->str-time]])
  (:import [red.device.device Device]
           [device.types MediaPayloadType MediaType]
           [device.info LoginAccount PlayInfo]
           [device.netsdk Sdk$Iface Notify$Iface]
           [clojure.lang Ref PersistentArrayMap Atom]
           [java.util UUID]
           [clojure.lang Keyword]
           [org.joda.time DateTime]))

(defprotocol ISource
  (connect [this])
  (add-client [this client id])
  (remove-client [this id])
  (source->device [this data]))

(deftype Source [^String       id
                 ^Sdk$Iface    device
                 ^String       manufacturer ;;厂商
                 ^LoginAccount account      ;;设备账号
                 ^PlayInfo     info
                 ^Ref          clients ;;(ref {id client ...})
                 ^Atom         header-data
                 ^Atom         device->flow
                 ^Atom         client->flow
                 ^DateTime     start-time]
  ISource
  (connect [this]
    (.StartMedia this info id nil))

  (add-client [this client id]
    (dosync
     (alter clients assoc id client)))

  (remove-client [this id]
    (dosync
     (when (empty? (alter clients dissoc id))
       (close this))))

  (source->device [this data]
    (.SendMediaData this data nil nil))

  IOperate
  (can-multiplex? [this args]
    "可复用的媒体,意味着实时流,且所有参数全相等"
    (let [[manufacturer* account* ^PlayInfo info*] args]
      (and (= MediaType/RealPlay (.type info) (.type info*))
           (= manufacturer manufacturer*)
           (= account account*)
           (= info info*))))

  (close [this]
    (dosync
     (log/info "close source")
     (remove-source device id)
     (.StopMedia this nil nil)
     (doseq [pclient (deref clients)]
       (close (val pclient)))))

  Sdk$Iface
  (StartMedia [this _ _ _]
    (.StartMedia device info id _))

  (StopMedia [this _ _]
    (.StopMedia device id _))

  (SendMediaData [this data _ _]
    (.SendMediaData device data id _))

  Notify$Iface
  (Connected [this _]
    (connect this))

  (Offline [this _]
    (dosync
     (doseq [pclient (deref clients)]
       (.Offline ^Notify$Iface (val pclient) _))))

  (MediaStarted [this _ _]
    (log/info "media start:" (str this))
    (dosync
     (doseq [pclient (deref clients)]
       (.MediaStarted ^Notify$Iface (val pclient) _ _))))

  (MediaFinish [this _ _]
    (dosync
     (doseq [pclient (deref clients)]
       (.MediaFinish ^Notify$Iface (val pclient) _ _))))

  (MediaData [this data _ _]
    (let [{:keys [^bytes payload type]} (bean data)]
      (swap! device->flow + (alength payload))
      (when (= type (.getValue MediaPayloadType/FileHeader))
        (reset! header-data data)))
    (doseq [pclient (deref clients)]
      (.MediaData ^Notify$Iface (val pclient) data _ _)))

  clojure.lang.IDeref
  (deref [_] @clients)

  Object
  (toString [_]
    (let [{:keys [channel stream_type connect_type start_time end_time]} (bean info)]
      (format "____source: %d %s %s %s %s, %d bytes\n%s"
              channel stream_type connect_type
              (Timeinfo->str-time start_time) (Timeinfo->str-time end_time)
              @device->flow
              (->> @clients vals (map str)
                   (clojure.string/join ",\n"))))))

(defn- create-source!
  "新建媒体源"
  [manufacturer ^LoginAccount account ^PlayInfo info]
  (dosync
   (let [{:keys [addr port]} (bean account)
         {:keys [channel stream_type connect_type start_time end_time]} (bean info)]
     (log/infof "create source: %s:%d %d %s %s %s"
                addr port channel connect_type start_time end_time))
   (let [device  (add-device! manufacturer account)
         id      (str (UUID/randomUUID))
         clients (ref {})
         source  (Source. id device manufacturer account info clients (atom nil) (atom 0) (atom 0) (now))]
     ;;将本source 添加入设备
     (add-source device source id)
     (connect source)
     ;;请求
     source)))

(defn get-all-sources []
  (dosync
   (reduce (fn [c pdevice]
             (->> pdevice val deref :sources (conj c)))
           {} (get-all-devices))))

(defn- can-source-multiplex?*
  "源能否复用"
  [manufacturer ^LoginAccount account ^PlayInfo info]
  (dosync
   (some (fn [psource]
           (let [^Source source (val psource)]
             (when (can-multiplex+? source manufacturer account info)
               source)))
         (get-all-sources))))

(defn get-source!
  "获取源,即生成执行程序并建立联系"
  [manufacturer ^LoginAccount account ^PlayInfo info]
  (dosync
   (if-let [source (can-source-multiplex?* manufacturer account info)]
     source
     (create-source! manufacturer account info))))
