(ns red.client.restfull
  (:require [red.utils :refer [now pass-mill]])
  (:import [java.util UUID Date]))

;;订阅列表
(defonce subscribes (ref {}))

(defn subscribe
  "订阅请求"
  [args]
  (let [session-id (UUID/randomUUID)
        current    (now)]
    (dosync
     (alter subscribes assoc session-id (assoc args :session-id session-id :regist-time current)))
    session-id))

(defn remove-subscribe
  "媒体请求成功之后,移除订阅项,返回详细信息"
  [^String session-id]
  (dosync
   (let [session (UUID/fromString session-id)
         subscribe (get session (deref subscribes))]
     (alter subscribes dissoc session))))

(defn check-timeout-task
  "处理请求session的超时"
  [] (let [timeout (* 30 1000)]
       (dosync
        (doseq [m (vals @subscribes)]
          (when (> (pass-mill (:regist-time m)) timeout)
            (-> (:session-id m) str remove-subscribe))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn test-data []
  (let [manufacturer "gt"
        addr  "192.168.8.63"
        port "8095"
        user  "user"
        password "pass"
        channel-id 0
        stream-type :main]
    (subscribe (assoc (correspond-args manufacturer addr port user password channel-id stream-type)
                 :session-type :realplay))))

;;(test-data)
;;(check-timeout-task)
;;subscribes
