(ns red.device.client.gateway
  "网关维护"
  (:require [clojure.tools.logging :as log]
            [red.device.client.device :refer [add-device! get-all-devices add-source remove-source]]
            [red.device.client.operate :refer :all]
            [red.utils :refer [now]])
  (:import [red.device.client.device Device]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [clojure.lang Ref PersistentArrayMap Fn Atom]
           [org.joda.time DateTime]
           [java.util UUID]))

(defprotocol IGateway)

(deftype Gateway [^String       id
                  ^Device       device
                  ^String       manufacturer ;;厂商
                  ^LoginAccount account      ;;设备账号
                  ^DateTime     start-time]
  IOperate
  (can-multiplex? [this args]
    (let [[account*] args]
      (= account* account)))

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

(defn- creat-gateway [])

(defn get-all-gateways []
  (dosync
   (reduce (fn [c pdevice]
             (->> pdevice val deref :gateways (conj c)))
           {} (get-all-devices))))

(defn get-gateway-by-id [^String id])

(defn close-gateway! [^Gateway gateway])

(defn open-gateway [])
