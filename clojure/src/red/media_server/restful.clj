(ns red.media-server.restful
  (:require [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [red.utils :refer [now pass-mill correspond-args]])
  (:import [java.util UUID Date]))

;;订阅列表
(defonce subscribes (ref {}))

(defn get-session-in-subscribes
  [^UUID session-id]
  (dosync
   (get (deref subscribes) session-id)))

(defn subscribe!
  "订阅请求"
  [args]
  (let [session-id (UUID/randomUUID)
        current    (now)]
    (dosync
     (alter subscribes assoc session-id (assoc args :session-id session-id :regist-time current)))
    session-id))

(defn get-and-remove-subscribe
  "媒体请求成功之后,移除订阅项,返回详细信息"
  [^UUID session-id]
  (dosync
   (let [subscribe (get (deref subscribes) session-id)]
     (alter subscribes dissoc session-id)
     subscribe)))

(defn check-timeout-task
  "处理请求session的超时"
  [] (let [timeout (* (env :subscribe-timeout 30) 1000)]
       (dosync
        (doseq [m (vals @subscribes)]
          (when (> (pass-mill (:regist-time m)) timeout)
            (log/info "media subscribe naver used in time, remove:" m)
            (-> (:session-id m) get-and-remove-subscribe))))))
