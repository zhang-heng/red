(ns red.device.client.sdk.callback
  (:require [thrift-clj.core :as thrift]
            [clojure.tools.logging :as log]
            [red.utils :refer [stack-trace]]
            [thrift-clj.transports :as t])
  (:import [device.netsdk Notify$Iface]
           [org.apache.thrift.server TThreadPoolServer TThreadPoolServer$Args]
           [org.apache.thrift.transport TServerSocket]
           [java.nio ByteBuffer]))

(thrift/import
 (:types [device.info MediaPackage])
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
    (future
      (try (io! (thrift/stop! server))
           (catch Exception e (log/warn e))))))

(defn try-do [f]
  (try (f)
       (catch Exception e (log/errorf "executor notify handle error: \n%s" (stack-trace e)))))

(defn start-thrift! [^Notify$Iface notifier]
  (let [handler (thrift/service Notify
                                (Test1 [mp] (log/debug "->sdk test1" mp))
                                (Test2 [bs] (log/debug "->sdk test2:" bs))
                                (Test3 [] (log/debug "->sdk test3") (device.info.MediaPackage.))
                                (Test4 [] (log/debug "->sdk test4") (ByteBuffer/allocate 0))

                                (Lanuched    [port] (try-do #(.Lanuched notifier port)))

                                (Connected   [device-id] (try-do #(.Connected notifier device-id)))

                                (Offline     [device-id] (try-do #(.Offline notifier device-id)))

                                (MediaFinish [media-id device-id] (try-do #(.MediaFinish notifier media-id device-id)))

                                (MediaData   [{:keys [type reserver ^bytes payload] :as ^MediaPackage data} media-id device-id]
                                             (try
                                               (let [d (device.info.MediaPackage.
                                                        type reserver (ByteBuffer/wrap payload))]
                                                 (.MediaData notifier d media-id device-id))
                                               (catch Exception e
                                                 (log/error e data media-id device-id)))))

        {:keys [server port]}  (multi-threaded-server handler 0
                                                      :bind "localhost"
                                                      :protocol :binary)]
    (io! (thrift/serve! server))
    (Thrift. server port)))
