(ns red.device.gateway
  "网关维护,与运维app对接,维护设备状态"
  (:require [clojure.tools.logging :as log]
            [red.device.device :refer [add-device! get-all-devices add-gateway]]
            [red.device.operate :refer :all]
            [red.utils :refer [now]])
  (:import [red.device.device Device]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [clojure.lang Ref PersistentArrayMap Fn Atom]
           [org.joda.time DateTime]
           [java.util UUID]))

(deftype Gateway [^String       id
                  ^Device       device
                  ;;运维订阅的通知
                  ^String       manufacturer ;;厂商
                  ^LoginAccount account      ;;设备账号
                  ^DateTime     start-time]
  IOperate
  (close [this])

  Sdk$Iface
  (GetVersion [this])
  (xDownloadRecordByFile [this])
  (xDownloadRecordByTime [this])
  (xStopDownload [this])

  Notify$Iface
  (Connected [this _])

  (Offline [this _])

  clojure.lang.IDeref
  (deref [_] )

  Object
  (toString [_]))

(defn- creat-gateway
  [manufacturer ^LoginAccount account]
  (let [device  (add-device! manufacturer account)
        id      (str (UUID/randomUUID))
        gateway (Gateway. id device manufacturer account (now))]
    (add-gateway device gateway)))

(defn get-all-gateways []
  (dosync
   (reduce (fn [c pdevice]
             (->> pdevice val deref :gateways (conj c)))
           {} (get-all-devices))))

(defn get-gateway-by-id [^String id])

(defn close-gateway! [^Gateway gateway])

(defn open-gateway
  [{:keys [manufacturer addr port user password]}]
  (let [account (LoginAccount. addr port user password)]
    (creat-gateway manufacturer account)))

(defn drop-gateway [id])
