(ns red.device.client.core
  "主动连接: socket (1->1) client (n->1) source (n->1) device (n*->1) exe"
  (:require [red.device.client.source :refer [get-all-sources get-source!]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref PersistentArrayMap)
           (org.joda.time DateTime)))

(defrecord Client [^PersistentArrayMap subscribe
                   ^Ref                flow<-device
                   ^Ref                flow->device
                   ^clojure.lang.Fn    dev->client
                   ^clojure.lang.Fn    dev->close
                   ^clojure.lang.Fn    dev<-client
                   ^clojure.lang.Fn    close<-client
                   ^DateTime           start-time])

(defn- mk-client*dev<-client
  "生成处理来自客户端数据的处理函数"
  [client source]
  (fn [buffer]
    (let [])))

(defn- mk-client*close<-client
  "生成来自客户端的关闭处理函数"
  [client source]
  (fn []))

(defn- mk-client*session
  "生成与客户端的相关数据"
  [{:keys [clients] :as source}
   subscribe dev->client dev->close]
  (let [client        (ref {})
        dev<-client   (mk-client*dev<-client client source)
        close<-client (mk-client*close<-client client source)]
    (dosync
     (alter clients conj client)
     (ref-set client (Client. subscribe
                              (ref 0) (ref 0)
                              dev->client dev->close dev<-client close<-client
                              (now)))
     (deref client))))

(defn get-all-clients []
  (dosync
   (let [{:keys [clients]} (get-all-sources)]
     (deref clients))))

(defn open-session!
  "处理请求,建立与设备的准备数据"
  [subscribe write-handle close-handle]
  (-> subscribe
      get-source!
      (mk-client*session subscribe write-handle close-handle)))
