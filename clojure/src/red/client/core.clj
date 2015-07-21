(ns red.client.core
  (:require [red.client.asynchronous-server :refer [run-server read-from write-to disconnect-notify]]
            [red.client.restfull :refer [get-and-remove-subscribe]]
            [red.device.active.core :refer [open-session*]])
  (:import [java.nio ByteBuffer charset.Charset]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]
           [java.util UUID Date]))

(defn- buffer->uuid [byte-buffer]
  (try
    (-> (Charset/forName "ASCII")
        (.decode byte-buffer)
        (str)
        (UUID/fromString))
    (catch Exception _ nil)))

(defn- mk-send-handler [connection]
  (fn [buffer] (write-to connection buffer)))

(defn- mk-close-handler [connection]
  (fn [] (let [{:keys [closer]} (deref connection)]
          (closer))))

(defn- payload-handler [connection payload-buffer]
  (let [{:keys [user]} (deref connection)]
    (@user payload-buffer)
    (read-from connection 4 header-handler)))

(defn- header-handler [connection size-buffer]
  (let [l (.getInt size-buffer)]
    (read-from connection l payload-handler)))

(defn- session-handler [connection session-buffer]
  (dosync
   (when-let [subscribe (-> session-buffer buffer->uuid get-and-remove-subscribe)]
     (let [{:keys [disconnector reader]} (open-session* subscribe (mk-send-handler) (mk-close-handler))
           {:keys [user]} (deref connection)]
       (disconnect-notify connection disconnector)
       (ref-set user reader)
       (read-from connection 4 header-handler)))))

(defn- accept-handler
  [connection]
  (read-from connection 36 session-handler))

(defn ^clojure.lang.Fn
  start-gtsp-server
  "启动监听服务,返回关闭函数"
  [host port]
  (run-server host port accept-handler))


;;run-server -> [client-connect]
;;                     ↓
;;               accept-handler -> read-connection
;;                                        ↓
;;                                 session-handler
;;                                        ↓  send/close-handler
;;                                 open-session*  -> receive-handler
;;                                        ↓
;;                      (read-connection -> receive-handler)
