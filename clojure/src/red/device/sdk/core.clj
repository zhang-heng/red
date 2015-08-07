(ns red.device.sdk.core
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [now]]
            [environ.core :refer [env]]
            [red.device.sdk.callback :refer [start-thrift!]]
            [red.device.sdk.launcher :refer [launch!]]
            [red.device.sdk.request :as req]
            [red.transobj :refer :all])
  (:import [clojure.lang Ref PersistentArrayMap Fn]
           [java.util UUID]
           [org.joda.time DateTime]))

;;exe复用个数
(defonce _MUX 2)

(defonce executors (ref #{}))

(defn login
  "设备登陆
  1.发生在执行程序建立之时,资源未必准备好,当资源启动成功,将未登录设备全数加载;
  2.在执行程序正常运行时,做直接请求"
  [{:keys [proc-thrift] :as executor}
   {{:keys [addr port user password]} :device-info :as device}]
  (req/login (deref proc-thrift 0 nil) addr port user password))

(defrecord Executor [^UUID     id           ;;执行程序唯一标识,方便检索
                     ^String   manufacturer ;;厂商
                     ^Ref      devices ;;进程所管理的设备 (ref {id device ...})
                     ^Ref      device->flow ;;来自设备的流量统计
                     ^Ref      client->flow ;;来自客户端的流量统计
                     ^Fn       proc-closer  ;;关闭进程方法
                     ^Object   proc-thrift ;;promise,当进程创建完毕并返回thrift参数
                     ^Fn       thrfit-closer ;;关闭本端thrift方法
                     ^Long     thrfit-port   ;;本端thrift端口
                     ^DateTime start-time]
  IClient
  (client->flow [this]
    (dosync
     (deref client->flow)))

  (client->login [this device-id]
    (dosync
     (when-let [device (get (deref devices) device-id)]
       (login this device))))

  (client->logout [this device-id]
    (req/logout (deref proc-thrift 0 nil) device-id))

  (client->data [this byte-buffer & [source-id device-id]]
    (req/voicedata-send (deref proc-thrift 0 nil) device-id source-id byte-buffer))

  (client->close [this & [source-id device-id]]
    (dosync
     ;; (req/voicedata-send (deref proc-thrift 0 nil) device-id source-id byte-buffer)
     ))

  IDevice
  (device->flow [this])
  (device->lanuched [this]
    (dosync
     (doseq [pdevice (deref devices)]
       (login this (val pdevice)))))

  (device->connected [this device-id]
    (dosync
     (if-let [device (get (deref devices) device-id)]
       ()
       (log/error "a device connected, but could not found in list"))))
  (device->offline [this device-id])
  (device->media-data [this media-type byte-buffer & id])
  (device->media-finish [this & id])
  (device->close [this])

  Object
  (toString [_] (map #(str (val %)) (deref devices))))






















;; (defrecord Executor [^UUID     executor-id
;;                      ^String   manufacturer
;;                      ^Ref      devices
;;                      ^Fn       proc-closer
;;                      ^Object   proc-thrift
;;                      ^Fn       thrfit-closer
;;                      ^Long     thrfit-port
;;                      ^DateTime start-time])

;; (declare get-all-executors)

;; (defn- can-exe-multiplex?*
;;   "创建执行程序"
;;   [manufacturer*]
;;   (dosync
;;    (some (fn [{:keys [devices manufacturer] :as executor}]
;;            (when (and (= manufacturer manufacturer*)
;;                       (< (count (deref devices)) _MUX))
;;              executor))
;;          (get-all-executors))))

;; (defn- find-device [executor-id device-id]
;;   (dosync
;;    (when-let [{:keys [devices]}
;;               (get (get-all-executors) executor-id)]
;;      (get (deref devices) device-id))))

;; (defn- mk-crashed [executor-id]
;;   (fn []
;;     (dosync
;;      (when-let [{:keys [devices proc-closer thrift-closer]} (get (get-all-executors) executor-id)]
;;        (proc-closer)
;;        (thrift-closer)
;;        (alter executors dissoc executor-id)
;;        (doseq [{:keys [device->offline]} (deref devices)]
;;          (device->offline))))))

;; (defn- mk-lanuched [executor-id]
;;   (fn [port]
;;     (dosync
;;      (when-let [{:keys [proc-thrift]} (get (get-all-executors) executor-id)]
;;        (deliver proc-thrift port)
;;        (req/init port)))))

;; (defn- mk-device->connected [executor-id]
;;   (fn [device-id]
;;     (dosync
;;      (when-let [{:keys [device->connected]} (find-device executor-id device-id)]
;;        (device->connected)))))

;; (defn- mk-device->offline [executor-id]
;;   (fn [device-id]
;;     (dosync
;;      (when-let [{:keys [device->offline]} (find-device executor-id device-id)]
;;        (device->offline)))))

;; (defn- mk-device->media-finish [executor-id]
;;   (fn [device-id source-id]
;;     (dosync
;;      (when-let [{:keys [device->media-finish]} (find-device executor-id device-id)]
;;        (device->media-finish source-id)))))

;; (defn- mk-device->media-data [executor-id]
;;   (fn [device-id source-id buffer]
;;     (dosync
;;      (when-let [{:keys [device->media-data]}
;;                 (find-device executor-id device-id)]
;;        (device->media-data source-id buffer)))))

;; (defn- mk-out-printer [manufacturer]
;;   (fn [pid string]
;;     (prn "<" manufacturer ", " pid ">: " string)))

;; (defn- create-process*thrift
;;   [manufacturer]
;;   (dosync
;;    (let [executor-id           (UUID/randomUUID)
;;          devices               (ref #{})
;;          {:keys [thrift-closer thrfit-port]}
;;          (start-thrift! (mk-lanuched executor-id)
;;                         (mk-device->connected executor-id)
;;                         (mk-device->offline executor-id)
;;                         (mk-device->media-finish executor-id)
;;                         (mk-device->media-data executor-id))

;;          sdk-path     (format "%s/%s" (System/getProperty "user.dir") (env :sdk-path))
;;          working-path (format "%s/%s" sdk-path manufacturer)
;;          exe-path     (format "%s/%s.exe" working-path manufacturer)
;;          proc-closer  (launch! (mk-crashed executor-id) (mk-out-printer manufacturer)
;;                                exe-path working-path
;;                                thrfit-port)
;;          proc-port    (promise)
;;          executor (Executor. executor-id manufacturer devices proc-closer proc-port thrift-closer thrfit-port (now))]
;;      (alter executors conj executor)
;;      executor)))

;; (defn login
;;   "args: executor device"
;;   [{:keys [proc-thrift]}
;;    {{:keys [addr port user password]} :device-info
;;     device->offline :device->offline}]
;;   (if-let [thrift-port (deref proc-thrift 3000 nil)]
;;     (req/login thrift-port addr port user password)
;;     (device->offline)))

;; (defn logout
;;   "args: executor device"
;;   [{:keys [proc-thrift]}
;;    {:keys [device-id]}]
;;   (req/logout proc-thrift device-id))

;; (defn client->device
;;   "args: executor device source-id buffer"
;;   [{:keys [proc-thrift]}
;;    {:keys [device-id]}
;;    source-id
;;    buffer]
;;   (req/voicedata-send proc-thrift source-id device-id))

;; (defn client->close
;;   "args: executor device source-id buffer"
;;   [{:keys [proc-thrift]}
;;    {:keys [device-id sources]}
;;    source-id]
;;   (dosync
;;    (when-let [{{:keys [session-type]} :subscribe} (get (deref sources) source-id)]
;;      (case session-type
;;        :realplay   (req/realplay-stop proc-thrift device-id source-id)
;;        :playback   (req/playback-stop proc-thrift device-id source-id)
;;        :voick-talk (req/voicetalk-stop proc-thrift device-id source-id)))))

;; (defn open-source
;;   "args: executor device source-id buffer"
;;   [{:keys [proc-thrift]}
;;    {:keys [device-id sources]}
;;    source-id]
;;   (dosync
;;    (when-let [{{:keys [session-type channel
;;                        stream-type start-time end-time]} :subscribe}
;;               (get (deref sources) source-id)]
;;      (case session-type
;;        :realplay   (req/realplay-start proc-thrift device-id source-id
;;                                        channel stream-type)
;;        :playback   (req/playback-bytime proc-thrift device-id source-id
;;                                         channel start-time end-time)
;;        :voick-talk (req/voicetalk-start proc-thrift device-id source-id
;;                                         channel)))))

;; (defn get-all-executors []
;;   (dosync
;;    (deref executors)))

;; (defn have-exe?
;;   "通过初始化扫描文件目录,获取sdk厂商列表"
;;   [manufacturer]
;;   (some? (#{"hik" "dahua"} manufacturer)))

;; (defn create-exe!
;;   "创建执行程序"
;;   [{:keys [manufacturer]}]
;;   (if-let [executor (can-exe-multiplex?* manufacturer)]
;;     executor
;;     (create-process*thrift manufacturer)))
