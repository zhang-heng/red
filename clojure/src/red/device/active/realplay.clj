(ns red.device.active.realplay
  (:require [red.device.active.core :refer [open-session*]])
  (:import [java.util UUID Date]
           [clojure.lang Ref Keyword IPersistentSet]))

(def sources #{})
(def clients #{})

;; (defrecord RealplaySource [^Ref            dvr-ref          ;;媒体源描述引用
;;                            ^Long           drayd-session-id ;;执行程序源引用
;;                            ^long           channel-id       ;;通道号
;;                            ^Keyword        stream-type      ;;通道类型 :main :sub
;;                            ^bytes          header-data      ;;媒体头
;;                            ^IPersistentSet client-sessions  ;;客户端列表
;;                            ^Date           data-time        ;;数据刷新时间
;;                            ^boolean        discarded?])
;;掉线

(defn- mk-receive-handler [subscribe]
  (fn [buffer]))

(defmethod open-session* :realplay [subscribe write-handle close-handle]
  (mk-receive-handler subscribe))
