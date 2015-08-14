(ns red.device.client.source
  (:require [clojure.tools.logging :as log]
            [red.device.client.device :refer [add-device! get-all-devices add-source]]
            [red.device.client.operate :refer :all]
            [red.utils :refer [now]])
  (:import [red.device.client.device Device]
           [device.types MediaType]
           [device.info LoginAccount PlayInfo]
           [device.netsdk Sdk$Iface Notify$Iface]
           [clojure.lang Ref PersistentArrayMap]
           [java.util UUID]
           [clojure.lang Keyword]
           [org.joda.time DateTime]))

(defprotocol ISource
  (client->device [this data]))

(deftype Source [^String       id
                 ^Device       device
                 ^String       manufacturer ;;厂商
                 ^LoginAccount account      ;;设备账号
                 ^Keyword      source-type ;; :playback :realplay :voicetalk
                 ^PlayInfo     info
                 ^Ref          clients
                 ^Ref          header-data
                 ^Ref          device->flow
                 ^Ref          client->flow
                 ^DateTime     start-time]
  ISource
  (client->device [this data]
    (case source-type
      :realplay  nil
      :playback  nil
      :voicetalk (.SendVoiceData this data nil nil)))

  IOperate
  (can-multiplex? [this args]
    "可复用的媒体,意味着实时流,且所有参数全相等"
    (let [[manufacturer* account* source-type* info*] args]
      (and (= :realplay source-type source-type*)
           (= manufacturer manufacturer*)
           (= account account*)
           (= info info*))))

  (sub-remove [this client]
    (dosync
     (log/info "remove client form source")
     (empty? (alter clients disj client)
             (case source-type
               :realplay  (.StopRealPlay  this nil nil)
               :playback  (.StopPlayBack  this nil nil)
               :voicetalk (.StopVoiceTalk this nil nil))
             (.close this))))

  (close [this]
    (dosync
     (log/info "close source")
     (doseq [^Notify$Iface client (deref clients)]
       (alter clients disj client)
       (.Offline client nil))
     (.sub-remove device this)))

  Sdk$Iface
  (StartRealPlay [this _ _ _]
    (.StartRealPlay device info id _))

  (StopRealPlay [this _ _]
    (.StopRealPlay device id _))

  (StartVoiceTalk [this _ _ _]
    (.StartVoiceTalk device info id _))

  (StopVoiceTalk [this _ _]
    (.StopVoiceTalk device id _))

  (SendVoiceData [this data _ _]
    (.SendVoiceData device data id _))

  (PlayBackByTime [this _ _ _]
    (.PlayBackByTime device info id _))

  (StopPlayBack [this _ _]
    (.StopPlayBack device id _))

  Notify$Iface
  (Connected [this _]
    (dosync
     (case source-type
       :playback  (.PlayBackByTime this info id _)
       :realplay  (.StartRealPlay  this info id _)
       :voicetalk (.StartVoiceTalk this info id _))))

  (Offline [this _]
    (dosync
     (doseq [client (deref clients)]
       (alter clients disj client)
       (.Offline ^Notify$Iface client _))))

  (MediaStarted [this _ _]
    (dosync
     (doseq [client (deref clients)]
       (.MediaStarted ^Notify$Iface client _ _))))

  (MediaFinish [this _ _]
    (dosync
     (doseq [client (deref clients)]
       (.MediaFinish ^Notify$Iface client _ _))))

  (MediaData [this {:keys [^ByteBuffer payload type] :as data} _ _]
    (dosync
     (when (= type MediaType/FileHeader)
       (ref-set header-data data))
     (doseq [client (deref clients)]
       (.MediaData ^Notify$Iface client data _ _))))

  clojure.lang.IDeref
  (deref [_] clients)

  Object
  (toString [_]
    (let [{:keys [channel stream_type connect_type start_time end_time]} (bean info)]
      (format "____source: %s %d %s %s %s %s \n%s"
              source-type channel stream_type connect_type start_time end_time
              (->> @clients
                   (map #(str %))
                   (clojure.string/join ",\n"))))))

(defn- create-source!
  "新建媒体源"
  [manufacturer ^LoginAccount account
   media-type   ^PlayInfo     info]
  (dosync
   (let [{:keys [addr port]} (bean account)
         {:keys [channel stream_type connect_type start_time end_time]} (bean info)]
     (log/infof "create source: %s:%d %d %s %s %s %s"
                addr port channel stream_type connect_type start_time end_time))
   (let [device  (add-device! manufacturer account)
         id      (str (UUID/randomUUID))
         clients (ref #{})
         source  (Source. id device manufacturer account media-type info clients (ref nil) (ref 0) (ref 0) (now))]
     ;;将本source 添加入设备
     (add-source device source)
     source)))

(defn get-all-sources []
  (dosync
   (reduce (fn [c device]
             (clojure.set/union c (deref (deref device))))
           #{} (get-all-devices))))

(defn- can-source-multiplex?*
  "源能否复用"
  [manufacturer ^LoginAccount account
   media-type   ^PlayInfo     info]
  (dosync
   (some (fn [^Source source]
           (when (can-multiplex+? source manufacturer account media-type info)
             source))
         (get-all-sources))))

(defn add-client
  [source client]
  (dosync
   (alter @source conj client)))

(defn get-source!
  "获取源,即生成执行程序并建立联系"
  [manufacturer ^LoginAccount account
   media-type   ^PlayInfo     info]
  (dosync
   (if-let [source (can-source-multiplex?* manufacturer account media-type info)]
     source
     (create-source! manufacturer account media-type info))))
