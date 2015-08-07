(ns red.media-server.core
  (:require [clojure.tools.logging :as log]
            [red.media-server.asynchronous-server :refer [run-server read-from write-to set-disconnect-notify
                                                    get-socket-info ]]
            [red.media-server.restful :refer [get-and-remove-subscribe]]
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

(defn- session-handler
  "接收到guid数据的处理"
  [connection session-buffer]
  (dosync
   (let [socket-info (-> connection deref :socket get-socket-info)
         closer      (-> connection deref :closer)
         string-uuid (buffer->string session-buffer)]
     (if-let [subscribe (-> (string->uuid string-uuid)
                            (get-and-remove-subscribe))]
       (let [{:keys [disconnector reader]} (open-session! subscribe (mk-send-handler connection) (mk-close-handler connection))
             {:keys [user]}                (deref connection)]
         (set-disconnect-notify connection disconnector)
         (ref-set user reader)
         (read-from connection 4 header-handler))
       (do (log/infof "received %s:%d a uuid not invalid: %s, close!"
                      (:remote-addr socket-info) (:remote-port socket-info) string-uuid)
           (closer))))))

(defn- payload-handler
  "接收到负载数据的处理"
  [connection payload-buffer]
  (let [{:keys [user closer]} (deref connection)
        {:keys [remote-addr remote-port]} (-> connection deref :socket get-socket-info)]
    (if (fn? @user)
      (@user payload-buffer)
      (do (log/errorf "receive frome %s:%s, but none of handle fun, close!")
          (closer)))
    (read-from connection 4 header-handler)))

(defn- header-handler
  "接收到头数据的处理"
  [connection ^ByteBuffer size-buffer]
  (let [l (.getInt size-buffer)]
    (read-from connection l payload-handler)))

(defn- accept-handler
  "处理网络请求"
  [connection]
  (let [{:keys [local-addr local-port remote-addr remote-port]}
        (-> connection deref :socket get-socket-info)]
    (log/infof "new connection come in now: %s:%d <- %s:%d" local-addr local-port remote-addr remote-port)
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
