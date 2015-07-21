(ns red.exe.callback
  (:require [thrift-clj.core :as thrift]))
(thrift/import
 (:types    [device.sdk.media MediaType MediaPackage])
 (:services [device.sdk.media Notify]))

;;定义方法:分离出来方便调试
(defn- launched [{{handle :launching} :handler}]
  (when handle
    (handle)))

(defn- offline [{{handle :offline} :handler}]
  (when handle
    (handle)))

(defn- media-finish [{{handle :media-finish} :handler}]
  (when handle
    (handle)))

(defn- media-data [{{handle :media-data} :handler} media]
  (when handle
    (handle media)))

;;监听请求
(defn- start-listen! [info port]
  (let [handler (thrift/service Notify
                                (Lanuched    []      (launched info))
                                (Offline     []      (offline info))
                                (MediaFinish []      (media-finish info))
                                (MediaData   [media] (media-data info media)))
        server (thrift/multi-threaded-server
                handler port :bind "localhost"
                :protocol :compact)]
    (thrift/serve! server)
    {:server server :port port :info info}))

(defn try-to-start-listen [info]
  (some (fn [port]
          (try
            (start-listen! info port)
            (catch org.apache.thrift.transport.TTransportException e nil)
            (catch Exception e (prn e))))
        (range 50000 60000)))

(defn stop-server! [{server :server}]
  (try (thrift/stop! server)
       (catch Exception e (prn e))))
