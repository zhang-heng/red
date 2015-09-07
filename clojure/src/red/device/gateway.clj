(ns red.device.gateway
  "网关维护,与运维app对接,维护设备状态"
  (:require [clojure.tools.logging :as log]
            [red.device.device :refer [add-device! get-all-devices set-gateway remove-gateway get-device-id get-device-status]]
            [red.device.operate :refer :all]
            [red.utils :refer [now correspond-args]])
  (:import [device.netsdk Sdk$Iface Notify$Iface]
           [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [clojure.lang Ref PersistentArrayMap Fn Atom]
           [org.joda.time DateTime]
           [java.util UUID]))

(defprotocol IGateway
  (reconnect [this]))

(deftype Gateway [^Sdk$Iface    device
                  ;;运维订阅的通知
                  ^String       manufacturer ;;厂商
                  ^LoginAccount account      ;;设备账号
                  ^DateTime     start-time]
  IGateway
  (reconnect [this]
    (when (= :offline (get-device-status device))
      (.Login device nil nil)))

  IOperate
  (close [this])

  Sdk$Iface
  (GetVersion [this]
    (.GetVersion device))

  (xDownloadRecordByFile [this])
  (xDownloadRecordByTime [this])
  (xStopDownload [this])

  Notify$Iface
  (Connected [this _])

  (Offline [this _])

  clojure.lang.IDeref
  (deref [_]
    (let [{:keys [addr port user password]} (bean account)]
      (correspond-args manufacturer addr port)))

  Object
  (toString [_]))

(defn get-all-gateways []
  (dosync
   (reduce (fn [c pdevice]
             (->> pdevice val deref (conj c)))
           {} (get-all-devices))))

(defn open-gateway
  [manufacturer addr port user password]
  (let [account (LoginAccount. addr port user password)
        device (add-device! manufacturer account)]
    (set-gateway device (Gateway. device manufacturer account (now)))
    (get-device-id device)))

(defn drop-gateway [device-id]
  (when-let [device (get (get-all-devices) device-id)]
    (remove-gateway device)))

(defn gateway-timer-task []
  (doseq [device (get-all-devices)]
    (when-let [gw (-> device val deref :gateway)])))
