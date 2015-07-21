(ns red.device.client.core
  "主动连接: socket (1->1) client (n->1) source (n->1) device (n*->1) exe"
  (:require [red.device.client.source :refer [get-all-sources get-source!]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref PersistentArrayMap)
           (org.joda.time DateTime)))

(defrecord Client [^PersistentArrayMap subscribe    ;;请求描述
                   ^Ref                flow<-device ;;来自设备流量统计
                   ^Ref                flow->device ;;发送至设备的流量统计
                   ^clojure.lang.Fn    dev->client
                   ^clojure.lang.Fn    dev->close
                   ^DateTime           start-time])

(defn- mk-client->dev
  [{:keys [flow->device]} {:keys [dev<-client]}]
  (fn [buffer]
    (dosync (alter flow->device +))
    (dev<-client buffer)))

(defn- mk-client->close
  [client {:keys [client->close]}]
  (fn [] (client->close client)))

(defn- mk-dev->client
  [flow->device {:keys [dev->client]}]
  (fn [buffer]
    (dosync (alter flow->device +))
    (dev->client buffer)))

(defn- mk-client
  "生成与客户端的相关数据"
  [{:keys [clients] :as source}
   subscribe dev->client dev->close]
  (let [flow->device (ref 0)
        client (Client. subscribe flow->device (ref 0) (mk-dev->client dev->client) dev->close (now))]
    (dosync
     (alter clients conj client)
     {:client->dev   (mk-client->dev client source)
      :client->close (mk-client->close client source)})))

(defn get-all-clients []
  (dosync
   (reduce (fn [c {clients :clients}] (clojure.set/union c))
           #{} (get-all-sources))))

(defn open-session!
  "处理请求,建立与设备的准备数据;返回数据发送函数和关闭函数"
  [subscribe write-handle close-handle]
  (-> subscribe
      get-source!
      (mk-client subscribe write-handle close-handle)))
