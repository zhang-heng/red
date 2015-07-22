(ns red.exe.callback
  (:require [thrift-clj.core :as thrift]
            [thrift-clj.gen.services :as s]
            [thrift-clj.transports :as t]
            [thrift-clj.protocol.core :as proto])
  (:import [org.apache.thrift.server
            TServer TSimpleServer
            TThreadPoolServer TNonblockingServer
            TServer$Args TServer$AbstractServerArgs
            TThreadPoolServer$Args TNonblockingServer$Args]
           [org.apache.thrift.transport TServerSocket TNonblockingServerSocket]
           [org.apache.thrift TProcessor]))


;;thrift-clj的接口中将port置为零,自动获取一个随机可用端口,不可查.
(defn- wrap-args
  ^TServer$AbstractServerArgs
  [^TServer$AbstractServerArgs base iface opts]
  (let [opts (apply hash-map opts)
        p (:protocol opts :compact)
        proto (if (keyword? p)
                (proto/protocol-factory p)
                (apply proto/protocol-factory p))]
    (doto base
      (.protocolFactory proto)
      (.processor (s/iface->processor iface)))))

(defn- multi-threaded-server
  ^TServer
  [iface port & opts]
  (let [t (t/blocking-server-transport port opts)]
    {:server (TThreadPoolServer. (wrap-args (TThreadPoolServer$Args. t) iface opts))
     :port (.. t getServerSocket getLocalPort)}))


(thrift/import
 (:types    [device.sdk.media MediaType MediaPackage])
 (:services [device.sdk.media Notify]))


(defn- launched [])

(defn- mk-stop-server [server]
  (fn []
    (try (thrift/stop! server)
         (catch Exception e (prn e)))))

(defn- start-listen! [{:keys [device->connected device->offline device->media-finish device->media-data]}
                      port]
  (let [handler (thrift/service Notify
                                (Lanuched    [] (Lanuched))
                                (Connected   [device-id] (device->connected device-id))
                                (Offline     [device-id] (device->offline device-id))
                                (MediaFinish [source-id] (device->media-finish source-id))
                                (MediaData   [source-id media] (device->media-data source-id media)))
        {:keys [server port]}  (multi-threaded-server handler port
                                                      :bind "localhost"
                                                      :protocol :compact)]
    (thrift/serve! server)
    {:closer (mk-stop-server server) :port port}))

(defn try-to-start-listen [info]
  (some (fn [port]
          (try
            (start-listen! info port)
            (catch org.apache.thrift.transport.TTransportException e nil)
            (catch Exception e (prn e))))
        (range 50000 60000)))
