(ns red.device.exe
  (:require [thrift-clj.core :as thrift]
            [red.device.operate :refer :all]
            [clojure.tools.logging :as log]
            [red.utils :refer [now stack-trace]]
            [red.config :refer [env]]
            [red.device.callback :refer [start-thrift!]]
            [red.device.launcher :refer [launch! check-proc-status]])
  (:import [red.device.callback Thrift]
           [red.device.launcher Proc]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.info InvalidOperation]
           [clojure.lang Ref PersistentArrayMap Fn]
           [java.nio ByteBuffer]
           [java.util UUID]
           [org.joda.time DateTime]))

(defonce ^:private executors (ref {}))

(thrift/import
 (:types    [device.types  MediaType    StreamType]
            [device.info   LoginAccount MediaPackage])
 (:clients  [device.netsdk Sdk]))

(defmacro request [port method & args]
  (let [method# (symbol "Sdk" (str method))]
    `(if (pos? ~port)
       (with-open [c# (thrift/connect! Sdk ["localhost" ~port] :protocol :binary)]
         (~method# c# ~@args))
       (log/error "sdk thrift port not found"))))

(defn get-all-executors []
  (dosync
   (deref executors)))

(defprotocol IExecutor
  (try-do [this f])
  (add-device [this device id] "添加设备")
  (remove-device [this id] "移除设备: 1.移除id设备; 2.判断设备列表为空则关闭本对象")
  (mk-out-printer [this] "创建用于sdk进程打印的函数")
  (mk-crashed [this] "创建用于处理进程close/crash的函数"))

(deftype Executor [^String   id           ;;执行程序唯一标识,方便检索
                   ^String   manufacturer ;;厂商
                   ^Ref      devices ;;进程所管理的设备 (ref {id device ...})
                   ^Ref      device->flow ;;来自设备的流量统计
                   ^Ref      client->flow ;;来自客户端的流量统计
                   ^Object   proc         ;;进程对象
                   ^Object   thrift-notify ;;本地thrift服务,接收来自sdk通知
                   ^Object   thrift-sdk ;;promise,当进程创建完毕并返回thrift参数
                   ^DateTime start-time]
  IExecutor
  (try-do [this f]
    (try (f)
         (catch InvalidOperation e (log/debug "sdk handle failed:" e))
         ;;除此之外,sdk出现故障,直接关闭
         (catch Exception e (log/warn "close executor:\n" this) (.close this))))

  (add-device [this device id]
    (dosync (alter devices assoc id device)))

  (remove-device [this id]
    (dosync
     (when (empty? (alter devices dissoc id))
       (close this))))

  (mk-out-printer [this]
    (fn [msg]
      (let [level :debug
            pid   (:pid (deref proc))
            header (format "%s<%d>" manufacturer pid)]
        (log/log header level nil msg))))

  (mk-crashed [this]
    (fn []
      (let [level :warn
            pid   (:pid (deref proc))
            header (format "%s<%d>" manufacturer pid)]
        (log/log header level nil "sdk proc crashed. notice all in exe resources"))
      ;;关闭释放当前
      (try (.close this)
           (catch Exception e (log/errorf "close executor resources: \n%s" (stack-trace e))))))

  IOperate
  (can-multiplex? [this args]
    (let [[manufacturer*] args
          mux             (env :exe-mux)]
      (and (= manufacturer manufacturer*)
           (if (pos? mux)
             (< (count (deref devices)) mux)
             true))))

  (close [this]
    (dosync
     ;;否则已经被删除了
     (when (get @executors id)
       (log/info "close executor")
       ;;从表中剔除此对象
       (alter executors dissoc id)
       ;;通知每个设备掉线
       (doseq [pdevice (deref devices)]
         (let [device-id (key pdevice)
               device    (val pdevice)]
           (alter devices dissoc device-id)
           (close device)))))
    ;;释放资源
    (future
      (try
        (let [^Proc proc (deref proc 2000 nil)]
          ;;关闭进程对象
          (.close proc))
        (catch Exception e (log/errorf "close executor proc: \n%s" (stack-trace e))))
      (try
        (let [^Thrift thrift (deref thrift-notify 1000 nil)]
          ;;关闭thrift本地监听
          (.close thrift))
        (catch Exception e (log/errorf "close executor thrift: \n%s" (stack-trace e))))))

  Sdk$Iface
  (Test1 [this mp] (log/debug "->sdk test1" (try-do this #(request @thrift-sdk Test1 (device.info.MediaPackage.)))))
  (Test2 [this bs] (log/debug "->sdk test2" (try-do this #(request @thrift-sdk Test2 (ByteBuffer/allocate 0)))))
  (Test3 [this] (log/debug "->sdk test3" (try-do this #(request @thrift-sdk Test3))))
  (Test4 [this] (log/debug "->sdk test4" (try-do this #(request @thrift-sdk Test4))))

  (GetVersion [this]
    (log/infof "%s version=%s"
               manufacturer (try-do this #(request @thrift-sdk GetVersion))))

  (InitSDK [this]
    (try-do this #(request @thrift-sdk InitSDK)))

  (CleanSDK [this]
    (try-do this #(request @thrift-sdk CleanSDK)))

  (Login [this account device-id]
    (future
      (try (request @thrift-sdk Login account device-id)
           (catch InvalidOperation e (log/info "login:" (.what e) (.why e)))
           (finally false))))

  (Logout [this device-id]
    (future
      (try-do this #(request @thrift-sdk Logout device-id))))

  (StartRealPlay [this info source-id device-id]
    (future
      (try-do this #(request @thrift-sdk StartRealPlay info source-id device-id))))
  (StopRealPlay [this source-id device-id]
    (future
      (try-do this #(request @thrift-sdk StopRealPlay source-id device-id))))

  (StartVoiceTalk [this info source-id device-id]
    (future
      (try-do this #(request @thrift-sdk StartVoiceTalk info source-id device-id))))
  (StopVoiceTalk [this source-id device-id]
    (try-do this #(request @thrift-sdk StopVoiceTalk source-id device-id)))
  (SendVoiceData [this data source-id device-id]
    (future
      (try-do this #(request @thrift-sdk SendVoiceData data source-id device-id))))

  (PlayBackByTime [this info source-id device-id]
    (future
      (try-do this #(request @thrift-sdk PlayBackByTime info source-id device-id))))
  (StopPlayBack [this source-id device-id]
    (future
      (try-do this #(request @thrift-sdk StopPlayBack source-id device-id))))

  Notify$Iface
  (Log [this msg]
    (let [level :info
          pid   (:pid (deref proc))
          header (format "%s<%d>" manufacturer pid)]
      (log/log header level nil msg)))

  (Lanuched [this port]
    (future
      (dosync
       (log/infof "process lanuched: port=%d" port)
       ;;保存sdk进程的thrift服务端口
       (deliver thrift-sdk port)
       ;;初始化进程
       (.InitSDK this)
       ;;告知所有设备进行访问操作
       (doseq [pdevice (deref devices)]
         (.Lanuched ^Notify$Iface (val pdevice) port)))))

  (Connected [this device-id]
    (dosync
     (if-let [^Notify$Iface device (get (deref devices) device-id)]
       (.Connected device device-id)
       (log/error "a device connected, but could not found in list"))))

  (Offline [this device-id]
    (dosync
     (if-let [^Notify$Iface device (get (deref devices) device-id)]
       (.Offline device device-id)
       (log/error "a device connected, but could not found in list"))))

  (MediaStarted [this media-id device-id]
    (dosync
     (if-let [^Notify$Iface device (get (deref devices) device-id)]
       (.MediaStarted device media-id device-id)
       (log/error "a device connected, but could not found in list"))))

  (MediaFinish [this media-id device-id]
    (dosync
     (if-let [^Notify$Iface device (get (deref devices) device-id)]
       (.MediaFinish device media-id device-id)
       (log/error "a device connected, but could not found in list"))))

  (MediaData [this data media-id device-id]
    (when-let [^Notify$Iface device (get (deref devices) device-id)]
      (.MediaData device data media-id device-id)))

  clojure.lang.IDeref
  (deref [_] @devices)

  Object
  (toString [_]
    (format "exe: %s \n%s"
            manufacturer
            (->> @devices
                 vals
                 (map str)
                 (clojure.string/join ",\n")))))

(defn- can-exe-multiplex?*
  "执行程序可否可复用"
  [manufacturer]
  (dosync
   (some (fn [pexecutor]
           (let [executor (val pexecutor)]
             (when (can-multiplex+? executor manufacturer)
               executor)))
         (get-all-executors))))

(defn- create-process*thrift
  [manufacturer]
  (dosync
   (log/info "create executor:" manufacturer)
   (let [id            (str (UUID/randomUUID))
         devices       (ref {})
         proc          (promise)
         thrift-notify (promise)
         thrift-sdk    (promise)
         sdk-path      (format "%s/%s" (System/getProperty "user.dir") (env :sdk-path))
         working-path  (format "%s/%s" sdk-path manufacturer)
         exe-path      (format "%s/%s.exe" working-path manufacturer)
         executor      (Executor. id manufacturer devices (ref 0) (ref 0) proc thrift-notify thrift-sdk (now))]
     ;;将当前对象添加入列表
     (alter executors assoc id executor)

     (send (agent nil)
           (fn [_]
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
               (catch Exception e (log/errorf "start local resource error: \n%s" (stack-trace e))))))
     executor)))

(defn have-exe?
  "通过初始化扫描文件目录,获取sdk厂商列表
  todo..."
  [manufacturer]
  (some? (#{"hik" "dahua"} manufacturer)))

(defn create-exe!
  "创建执行程序"
  [manufacturer]
  (if-let [executor (can-exe-multiplex?* manufacturer)]
    executor
    (create-process*thrift manufacturer)))

(defn clean-executors []
  (dosync
   (doseq [pexecutor (deref executors)]
     (let [executor (val pexecutor)
           executor-id (key pexecutor)]
       (alter executors dissoc executor-id)
       (close executor)))))
