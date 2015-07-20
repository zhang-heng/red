(ns red.client.core
  (:require [red.client.asynchronous-server :refer [run-server read-from write-to]]
            [red.client.restfull :refer [get-and-remove-subscribe]]
            [red.device.active.core :refer [open-session*]]
            [red.device.active.realplay]
            [red.device.active.playback]
            [red.device.active.voicetalk])
  (:import [java.nio ByteBuffer charset.Charset]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]
           [java.util UUID Date]))



;; (defn- uuid [byte-buffer]
;;   (try
;;     (-> (Charset/forName "ASCII")
;;         (.decode byte-buffer)
;;         (str)
;;         (UUID/fromString))
;;     (catch Exception _ nil)))

;; (defn- open-session
;;   [connection {:keys [session-type] :as subscribe}]
;;   (case session-type
;;     :realplay (open-realplay connection subscribe)
;;     :playback (open-playback connection subscribe)
;;     :voicetalk (open-voicetalk connection subscribe)))

;; (defn- accept-handler
;;   "处理新连接,返回准备接受UUID的36字长"
;;   [connection]
;;   (dosync
;;    (let [{user :user}  connection]
;;      (ref-set user :session)
;;      36)))

;; (defmulti receive-handler* (fn [connection] (deref (:user connection))))

;; (defmethod receive-handler* :session [{:keys [user] :as connection} buffer]
;;   (dosync
;;    (when-let [{:keys [session-id] :as subscribe}
;;               (-> buffer uuid get-and-remove-subscribe)]
;;      (open-session connection subscribe)
;;      (ref-set user :payload)
;;      4)))

;; (defmethod receive-handler* :header [{:keys [user] :as connection} buffer]
;;   (dosync
;;    (let [len (.getInt buffer)]
;;      (when (< len (* 1024 1024))
;;        (ref-set user :payload)
;;        len))))

;; (defmethod receive-handler* :payload [{:keys [user] :as connection} buffer]
;;   (dosync
;;    (ref-set user :header)
;;    2))

;; (defn stop []
;;   (@server))



;;core.clj

(defn- mk-receive-header-handler [connection])
(defn- mk-receive-payload-handler [connection])

(defn- mk-send-handler [connection]
  (fn [buffer] (write-to connection buffer)))

(defn- mk-close-handler [connection])

(defn- session-handler [connection session-buffer]
  (let [subscribe nil
        receiver (open-session* subscribe (mk-send-handler) (mk-close-handler))]))

(defn- accept-handler [connection]
  (let [buffer nil]
    (read-from connection buffer session-handler)))

(defn ^clojure.lang.Fn
  start
  "启动监听服务,返回关闭函数"
  [host port]
  (run-server host port accept-handler))


;;server.clj

;;realplay







;;run-server -> [client-connect]
;;                     ↓
;;               accept-handler -> read-connection
;;                                        ↓
;;                                 session-handler
;;                                        ↓  send/close-handler
;;                                 open-session*  -> receive-handler
;;                                        ↓
;;                      (read-connection -> receive-handler)
