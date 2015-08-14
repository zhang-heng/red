(ns red.device.client.sdk.core
  (:require [thrift-clj.core :as thrift]
            [red.device.client.operate :refer :all]
            [clojure.tools.logging :as log]
            [red.utils :refer [now stack-trace]]
            [environ.core :refer [env]]
            [red.device.client.sdk.callback :refer [start-thrift!]]
            [red.device.client.sdk.launcher :refer [launch! check-proc-status]])
  (:import [red.device.client.sdk.callback Thrift]
           [red.device.client.sdk.launcher Proc]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.info InvalidOperation]
           [clojure.lang Ref PersistentArrayMap Fn]
           [java.nio ByteBuffer]
           [java.util UUID]
           [org.joda.time DateTime]))

;;exe复用个数
(defonce _MUX 2)

(defonce ^:private executors (ref #{}))

(thrift/import
 (:types    [device.types  MediaType    StreamType]
            [device.info   LoginAccount MediaPackage])
 (:clients  [device.netsdk Sdk]))

(defn try-do [f]
  (try (f)
       (catch InvalidOperation e (prn e))))

(defmacro request [port method & args]
  (let [method# (symbol "Sdk" (str method))]
    `(if (pos? ~port)
       (with-open [c# (thrift/connect! Sdk ["localhost" ~port] :protocol :binary)]
         (try-do #(~method# c# ~@args)))
       (log/error "sdk thrift port not found"))))

(defn get-all-executors []
  (dosync
   (deref executors)))

(defn get-device [executor device-id]
  (dosync (some (fn [device] (when (is-you? device device-id) device))
                (deref (deref executor)))))

(deftype Executor [^String   id           ;;执行程序唯一标识,方便检索
                   ^String   manufacturer ;;厂商
                   ^Ref      devices ;;进程所管理的设备 (ref {id device ...})
                   ^Ref      device->flow ;;来自设备的流量统计
                   ^Ref      client->flow ;;来自客户端的流量统计
                   ^Object   proc         ;;进程对象
                   ^Object   thrift-notify ;;本地thrift服务,接收来自sdk通知
                   ^Object   thrift-sdk ;;promise,当进程创建完毕并返回thrift参数
                   ^DateTime start-time]
  IOperate
  (can-multiplex? [this args]
    (let [[manufacturer*] args]
      (and (= manufacturer manufacturer*)
           (< (count (deref devices)) _MUX))))

  (sub-remove [this device]
    (dosync
     (log/info "remove device form executor")
     (empty? (alter devices disj device)
             (.close this))))

  (close [this]
    (dosync
     (log/info "close executor")
     ;;通知每个设备掉线
     (doseq [^Notify$Iface device (deref devices)]
       (alter devices disj device)
       (.Offline device nil))
     ;;从表中剔除此对象
     (alter executors disj this)
     ;;释放资源
     (future
       (try
         (let [^Proc   proc   (deref proc 100 nil)
               ^Thrift thrift (deref thrift-notify 100 nil)]
           ;;关闭进程对象
           (.close proc)
           ;;关闭thrift本地监听
           (.close thrift))
         (catch Exception e (log/errorf "close executor resources: \n%s" (stack-trace e)))))))

  (exe-log [this msg]
    (let [level :debug
          pid   (:pid (deref proc))
          header (format "%s<%d>" manufacturer pid)]
      (log/log header level nil msg)))

  Sdk$Iface
  (GetVersion [this]
    (log/infof "%s version=%s" manufacturer (request @thrift-sdk GetVersion)))

  (InitSDK [this]
    (request @thrift-sdk InitSDK))

  (CleanSDK [this]
    (request @thrift-sdk CleanSDK))

  (Login [this account device-id]
    (some? (try (request @thrift-sdk Login account device-id)
                (catch InvalidOperation e (log/info "login:" (.what e) (.why e)))
                (catch Exception e (log/info "login unknow:" e))
                (finally false))))

  (Logout [this device-id]
    (some? (request @thrift-sdk Logout device-id)))

  (StartRealPlay [this info source-id device-id]
    (some? (request @thrift-sdk StartRealPlay info source-id device-id)))
  (StopRealPlay [this source-id device-id]
    (some? (request @thrift-sdk StopRealPlay source-id device-id)))

  (StartVoiceTalk [this info source-id device-id]
    (some? (request @thrift-sdk StartVoiceTalk device-id source-id info)))
  (StopVoiceTalk [this source-id device-id]
    (some? (request @thrift-sdk StopVoiceTalk source-id device-id)))
  (SendVoiceData [this data source-id device-id]
    (some? (request @thrift-sdk SendVoiceData data source-id device-id)))

  (PlayBackByTime [this info source-id device-id]
    (some? (request @thrift-sdk PlayBackByTime info source-id device-id)))
  (StopPlayBack [this source-id device-id]
    (some? (request @thrift-sdk StopPlayBack source-id device-id)))

  Notify$Iface
  (Lanuched [this port]
    (dosync
     (log/infof "process lanuched: port=%d \n%s" port this)
     (deliver thrift-sdk port)
     (.InitSDK this)
     (.GetVersion this)
     (doseq [^Notify$Iface device (deref devices)]
       (.Lanuched device port))))

  (Connected [this device-id]
    (log/error "a device connected" device-id)
    (dosync
     ;; (if-let [^Notify$Iface device (deref (deref devices) device-id)]
     ;;   (.Connected device device-id)
     ;;   (log/error "a device connected, but could not found in list"))
     ))

  (Offline [this device-id]
    (dosync
     ;; (if-let [^Notify$Iface device (get (deref devices) device-id)]
     ;;   (.Offline device device-id)
     ;;   (log/error "a device connected, but could not found in list"))
     ))

  (MediaStarted [this media-id device-id]
    (dosync
     ;; (if-let [^Notify$Iface device (get (deref devices) device-id)]
     ;;   (.MediaStarted device media-id device-id)
     ;;   (log/error "a device connected, but could not found in list"))
     ))

  (MediaFinish [this media-id device-id]
    (dosync
     ;; (if-let [^Notify$Iface device (get (deref devices) device-id)]
     ;;   (.MediaFinish device media-id device-id)
     ;;   (log/error "a device connected, but could not found in list"))
     ))

  (MediaData [this data media-id device-id]
    (dosync
     ;; (if-let [^Notify$Iface device (get (deref devices) device-id)]
     ;;   (.MediaData device data media-id device-id)
     ;;   (log/error "a device connected, but could not found in list"))
     ))

  clojure.lang.IDeref
  (deref [_] devices)

  Object
  (toString [_]
    (format "exe: %s \n%s"
            manufacturer
            (->> @devices
                 (map #(str %))
                 (clojure.string/join ",\n")))))

(defn- can-exe-multiplex?*
  "执行程序可否复用"
  [manufacturer]
  (dosync
   (some (fn [executor]
           (when (can-multiplex+? executor manufacturer)
             executor))
         (deref executors))))

(defn- mk-crashed [^Executor executor]
  (fn []
    (.exe-log executor "crashed. close all in exe resources")
    (.exe-log executor (str executor))
    (try (.close executor)
         (catch Exception e (log/errorf "close executor resources: \n%s" (stack-trace e))))))

(defn- mk-out-printer [^Executor executor]
  (fn [msg]
    (exe-log executor msg)))

(defn- create-process*thrift
  [manufacturer]
  (dosync
   (log/info "create executor:" manufacturer)
   (let [executor-id   (str (UUID/randomUUID))
         devices       (ref #{})
         proc          (promise)
         thrift-notify (promise)
         thrift-sdk    (promise)
         sdk-path      (format "%s/%s" (System/getProperty "user.dir") (env :sdk-path))
         working-path  (format "%s/%s" sdk-path manufacturer)
         exe-path      (format "%s/%s.exe" working-path manufacturer)
         executor      (Executor. executor-id manufacturer devices (ref 0) (ref 0) proc thrift-notify thrift-sdk (now))]
     ;;将当前对象添加入列表
     (alter executors conj executor)

     (future
       (try
         (do ;;;启动资源
           ;;启动本地thrift监听
           (log/debug "start waitting thrift connection from sdk process.")
           (deliver thrift-notify (start-thrift! executor))
           (log/debug "launch process ")
           ;;启动sdk进程
           (deliver proc (launch! (mk-out-printer executor) (mk-crashed executor)
                                  exe-path working-path
                                  (.get-port ^Thrift @thrift-notify))))
         (catch Exception e (log/errorf "start local resource error: \n%s" (stack-trace e)))))
     executor)))

(defn have-exe?
  "通过初始化扫描文件目录,获取sdk厂商列表"
  [manufacturer]
  (some? (#{"hik" "dahua"} manufacturer)))

(defn add-device
  [executor device]
  (dosync
   (alter @executor conj device)))

(defn create-exe!
  "创建执行程序"
  [manufacturer]
  (if-let [executor (can-exe-multiplex?* manufacturer)]
    executor
    (create-process*thrift manufacturer)))

(defn clean-executors []
  (dosync
   (doseq [^Executor executor (deref executors)]
     (close executor))
   (ref-set executors #{})))
