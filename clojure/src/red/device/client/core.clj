(ns red.device.client.core
  "主动连接: socket (1->1) client (n->1) source (n->1) device (n*->1) exe"
  (:require [red.device.client.source :refer [get-all-sources get-source!]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref PersistentArrayMap)
           (org.joda.time DateTime)))

(defrecord Client [^PersistentArrayMap subscribe      ;;请求描述
                   ^Ref                device->flow   ;;来自设备流量统计
                   ^Ref                client->flow   ;;发送至设备的流量统计
                   ^clojure.lang.Fn    device->client ;;设备发往客户端的操作
                   ^clojure.lang.Fn    device->close  ;;来自设备关闭通知
                   ^clojure.lang.Fn    client->dev
                   ^clojure.lang.Fn    client->close
                   ^DateTime           start-time])

(defn- mk-client->device
  [client->device client->flow]
  (fn [buffer]
    (dosync (alter client->flow + (.limit buffer)))
    (client->device  buffer)))

(defn- mk-client->close
  [client->close source]
  (fn [] (client->close)))

(defn- mk-device->client
  [device->client device->flow]
  (fn [buffer]
    (dosync (alter device->flow + (.limit buffer)))
    (device->client buffer)))

(defn- mk-device-close
  [device->close]
  (fn []
    device->close))

(defn- mk-client
  "生成与客户端的相关数据"
  [{:keys [clients client->device client->close] :as source}
   subscribe device->client device->close]
  (let [device->flow (ref 0)
        client->flow (ref 0)
        client (Client. subscribe device->flow client->flow
                        (mk-device->client device->client device->flow)
                        (mk-device-close device->close)
                        (mk-client->device client->device client->flow )
                        (mk-client->close client->close source )
                        (now))]
    (dosync
     (alter clients conj client)
     client)))

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
