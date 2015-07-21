(ns red.device.client.core
  "主动连接: socket (1->1) client (n->1) source (n->1) device (n*->1) exe"
  (:require [red.device.client.source :refer [get-all-sources get-source!]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref PersistentArrayMap)
           (org.joda.time DateTime)))

(defrecord Client [^PersistentArrayMap subscribe    ;;请求描述
                   ^Ref                device->flow ;;来自设备流量统计
                   ^Ref                client->flow ;;发送至设备的流量统计
                   ^clojure.lang.Fn    dev->client  ;;设备发往客户端的操作
                   ^clojure.lang.Fn    dev->close   ;;来自设备关闭通知
                   ^DateTime           start-time])

(defn- mk-client->dev
  [client->flow client->dev]
  (fn [buffer]
    (dosync (alter client->flow +))
    (client->dev  buffer)))

(defn- mk-client->close
  [client client->close]
  (fn [] (client->close client)))

(defn- mk-dev->client
  [dev->client device->flow]
  (fn [buffer]
    (dosync (alter device->flow +))
    (dev->client buffer)))

(defn- mk-client
  "生成与客户端的相关数据"
  [{:keys [clients client->dev client->flow client->close] :as source}
   subscribe dev->client dev->close]
  (let [device->flow (ref 0)
        client->flow (ref 0)
        client (Client. subscribe device->flow client->flow
                        (mk-dev->client dev->client device->flow) dev->close
                        (now))]
    (dosync
     (alter clients conj client)
     {:client->dev   (mk-client->dev client->flow client->dev)
      :client->close (mk-client->close client client->close)})))

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
