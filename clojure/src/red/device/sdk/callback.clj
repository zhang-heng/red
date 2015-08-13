(ns red.device.sdk.callback
  (:require [thrift-clj.core :as thrift]
            [thrift-clj.transports :as t])
  (:import [device.netsdk Notify$Iface]
           [org.apache.thrift.server TThreadPoolServer TThreadPoolServer$Args]
           [org.apache.thrift.transport TServerSocket]))

(thrift/import
 (:services [device.netsdk Notify]))

;;thrift-clj的接口中将port置为零,自动获取一个随机可用端口,不可查.
(defonce wrap-args (deref #'thrift-clj.server/wrap-args))
(defn- multi-threaded-server
  [iface port & opts]
  (let [t (t/blocking-server-transport port opts)]
    {:server (TThreadPoolServer. (wrap-args (TThreadPoolServer$Args. t) iface opts))
     :port (.. t getServerSocket getLocalPort)}))

(defprotocol IThrfit
  (get-port [this])
  (close [this]
    (io!)))

(defrecord Thrift [server port]
  IThrfit
  (get-port [this]
    port)
  (close [this]
    (try (thrift/stop! server)
         (catch Exception e (prn e)))))

(defn start-thrift! [^Notify$Iface notifier]
  (let [handler (thrift/service Notify
                                (Lanuched    [port] (.Lanuched notifier port))
                                (Connected   [device-id] (.Connected notifier device-id))
                                (Offline     [device-id] (.Offline notifier device-id))
                                (MediaFinish [device-id media-id] (.MediaFinish notifier device-id media-id))
                                (MediaData   [device-id media-id data] (.MediaData notifier device-id media-id data)))
        {:keys [server port]}  (multi-threaded-server handler 0
                                                      :bind "localhost"
                                                      :protocol :compact)]
    (io! (thrift/serve! server))
    (Thrift. server port)))