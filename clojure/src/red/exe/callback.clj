(ns red.exe.callback
  (:require [thrift-clj.core :as thrift]))
(thrift/import
 (:types    [device.sdk.media eMediaType MediaPackage])
 (:services [device.sdk.media Notify]))

;;定义方法:分离出来方便调试
(defn- launched [{{handle :launching} :handler}]
  (handle))

(defn- offline [{{handle :offline} :handler}]
  (handle))

(defn- media-finish [{{handle :media-finish} :handler}]
  (handle))

(defn- media-data [{{handle :media-data} :handler} media]
  (handle media))

;;监听请求
(defn- start-listen [args port]
  (try
    (let [handler (thrift/service Notify
                                  (Lanuched    []      (lanuched args))
                                  (Offline     []      (offline args))
                                  (MediaFinish []      (media-finish args))
                                  (MediaData   [media] (media-data args media)))
          server (thrift/multi-threaded-server
                  handler port :bind "localhost"
                  :protocol :compact)]
      (thrift/serve! server)
      {:server server :port port :args args})
    (catch org.apache.thrift.transport.TTransportException e nil)
    (catch Exception e (prn e))))


(defn try-to-start-listen
  [args]
  (let [server (some (fn [port]
                       (start-listen args port))
                     (range 50000 60000))]
    server))


(defn stop-server [{server :server}]
  (thrift/stop! server))
