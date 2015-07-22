(ns red.exe.core
  (:require [red.exe.callback :refer [try-to-start-listen stop-server!]]
            [red.exe.launcher :refer [launch!]])
  (:import [clojure.lang Ref Fn]
           [org.joda.time DateTime]))

;;exe复用个数
(defonce _MUX 2)

(defrecord Thrift [])

(defrecord Executor [^Process  proc
                     ^Ref      devices
                     ^Thrift   thrift
                     ^String   describe ;;厂商描述
                     ^String   path     ;;路径
                     ^DateTime start-time])

(defonce executors (ref #{}))

(defonce locker (Object.))


(defn login
  [device-info])

(defn logout
  [source-id])

(defn get-all-executors []
  (dosync
   (deref executors)))

(defn can-exe-multiplex?*
  "创建执行程序"
  [])

(defn create-exe!
  "创建执行程序"
  [media-info]
  (locking locker
    (when-let [{:keys [server port info] :as cb}
               (try-to-start-listen media-info)]
      (if-let [proc (launch! 'path 'name port '(proc-loger media-info))]
        (assoc cb :process proc)
        (do (stop-server! server)
            (prn "error"))))))
