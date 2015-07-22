(ns red.exe.callback
  (:require [thrift-clj.core :as thrift]
            [thrift-clj.transports :as t])
  (:import [org.apache.thrift.server TThreadPoolServer TThreadPoolServer$Args]
           [org.apache.thrift.transport TServerSocket]))

(thrift/import
 (:types    [device.sdk.media MediaType MediaPackage])
 (:services [device.sdk.media Notify]))

;;thrift-clj的接口中将port置为零,自动获取一个随机可用端口,不可查.
(defonce wrap-args (deref #'thrift-clj.server/wrap-args))
(defn- multi-threaded-server
  [iface port & opts]
  (let [t (t/blocking-server-transport port opts)]
    {:server (TThreadPoolServer. (wrap-args (TThreadPoolServer$Args. t) iface opts))
     :port (.. t getServerSocket getLocalPort)}))

(defn- launched [])

(defn- mk-stop-server [server]
  (fn []
    (try (thrift/stop! server)
         (catch Exception e (prn e)))))

(defn start-listen! [{:keys [device->connected device->offline device->media-finish device->media-data]}]
  (let [handler (thrift/service Notify
                                (Lanuched    [] (Lanuched))
                                (Connected   [device-id] (device->connected device-id))
                                (Offline     [device-id] (device->offline device-id))
                                (MediaFinish [source-id] (device->media-finish source-id))
                                (MediaData   [source-id media] (device->media-data source-id media)))
        {:keys [server port]}  (multi-threaded-server handler 0
                                                      :bind "localhost"
                                                      :protocol :compact)]
    (thrift/serve! server)
    {:closer (mk-stop-server server) :port port}))
