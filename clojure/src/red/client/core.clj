(ns red.client.core
  (:require [clojure.tools.logging :as log]
            [red.client.asynchronous-server :refer [run-server read-from write-to disconnect-notify
                                                    get-socket-info ]]
            [red.client.restful :refer [get-and-remove-subscribe]]
            [red.device.client.core :refer [open-session!]]
            [red.utils :refer [string->uuid buffer->string]])
  (:import [java.nio ByteBuffer charset.Charset]
           [java.nio.channels AsynchronousSocketChannel]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]
           [java.util UUID Date]))

(defn- mk-send-handler [connection]
  (fn [buffer] (write-to connection buffer)))

(defn- mk-close-handler [connection]
  (fn [] (let [{:keys [closer]} (deref connection)]
          (closer))))

(declare session-handler header-handler payload-handler)

(defn- session-handler [connection session-buffer]
  (dosync
   (let [socket-info (-> connection deref :socket get-socket-info)
         closer      (-> connection deref :closer)
         string-uuid (buffer->string session-buffer)]
     (if-let [subscribe (-> (string->uuid string-uuid)
                            (get-and-remove-subscribe))]
       (let [{:keys [disconnector reader]} (open-session! subscribe (mk-send-handler) (mk-close-handler))
             {:keys [user]}                (deref connection)]
         (disconnect-notify connection disconnector)
         (ref-set user reader)
         (read-from connection 4 header-handler))
       (do (log/infof "received %s:%d a uuid not invalid: %s, close!"
                      (:remote-addr socket-info) (:remote-port socket-info) string-uuid)
           (closer))))))

(defn- payload-handler [connection payload-buffer]
  (let [{:keys [user]} (deref connection)]
    (@user payload-buffer)
    (read-from connection 4 header-handler)))

(defn- header-handler
  [connection ^ByteBuffer size-buffer]
  (let [l (.getInt size-buffer)]
    (read-from connection l payload-handler)))

(defn- accept-handler
  [connection]
  (let [{:keys [socket]} (deref connection)
        {:keys [local-addr local-port remote-addr remote-port]} (get-socket-info socket)]
    (log/info "new connection come in now: %s:%d <- %s:%d" local-addr local-port remote-addr remote-port)
    (read-from connection 36 session-handler)))

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
