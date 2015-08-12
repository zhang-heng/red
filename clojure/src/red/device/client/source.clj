(ns red.device.client.source
  (:require [red.device.client.device :refer [add-device! get-all-devices add-source]]
            [red.utils :refer [now]])
  (:import [red.device.client.device Device]
           [device.types MediaType]
           [device.info LoginAccount PlayInfo]
           [device.netsdk Sdk$Iface Notify$Iface]
           [clojure.lang Ref PersistentArrayMap]
           [java.util UUID]
           [clojure.lang Keyword]
           [org.joda.time DateTime]))

(defrecord Source [^UUID     id
                   ^Device   device
                   ^Keyword  source-type ; :playback :realplay :voicetalk
                   ^PlayInfo info
                   ^Ref      clients
                   ^Ref      header-data
                   ^Ref      device->flow
                   ^Ref      client->flow
                   ^DateTime start-time]
  Sdk$Iface
  (StartRealPlay [this info _ _]
    (.StartRealPlay device info id _))

  (StopRealPlay [this _ _]
    (.StopRealPlay device id _))

  (StartVoiceTalk [this info _ _]
    (.StartVoiceTalk device info id _))

  (StopVoiceTalk [this _ _]
    (.StopVoiceTalk device id _))

  (SendVoiceData [this data _ _]
    (.SendVoiceData device data id _))

  (PlayBackByTime [this info _ _]
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
     (doseq [pclient (deref clients)]
       (.Offline ^Notify$Iface (val pclient) _))))

  (MediaStarted [this _ _]
    (dosync
     (doseq [pclient (deref clients)]
       (.MediaStarted ^Notify$Iface (val pclient) _ _))))

  (MediaFinish [this _ _]
    (dosync
     (doseq [pclient (deref clients)]
       (.MediaFinish ^Notify$Iface (val pclient) _ _))))

  (MediaData [this {:keys [^ByteBuffer payload type] :as data} _ _]
    (dosync
     (when (= type MediaType/FileHeader)
       (ref-set header-data data))
     (doseq [pclient (deref clients)]
       (.MediaData ^Notify$Iface (val pclient) data _ _))))

  Object
  (toString [_]))

(defn- create-source!
  "新建媒体源"
  [manufacturer ^LoginAccount account
   media-type   ^PlayInfo     info]
  (dosync
   (let [device  (add-device! manufacturer account)
         id      (UUID/randomUUID)
         clients (ref {})
         source  (Source. id device media-type info clients (ref nil) (ref 0) (ref 0) (now))]
     ;;将本source 添加入设备
     (add-source device source)
     source)))

(defn get-all-sources []
  (dosync
   (reduce (fn [c {sources :sources}]
             (clojure.set/union c (vals (deref sources))))
           #{} (get-all-devices))))

(defn- can-source-multiplex?*
  "源能否复用"
  [manufacturer ^LoginAccount account
   media-type   ^PlayInfo     info]
  (dosync
   (some (fn [{{manufacturer* :manufacturer
               {account* :account} :executor} :device
               info* :info
               media-type* :source-type
               :as source}]
           (when (and (= :realplay media-type media-type*)
                      (= manufacturer manufacturer*)
                      (= account account*)
                      (= info info*))
             source))
         (get-all-sources))))

(defn add-client
  [{:keys [clients] :as source}
   {:keys [id]      :as client}]
  (alter clients assoc id client))

(defn get-source!
  "获取源,即生成执行程序并建立联系"
  [manufacturer ^LoginAccount account
   media-type   ^PlayInfo     info]
  (dosync
   (if-let [source (can-source-multiplex?* manufacturer account media-type info)]
     source
     (create-source! manufacturer account media-type info))))
