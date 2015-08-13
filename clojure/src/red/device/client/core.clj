(ns red.device.client.core
  "主动连接: socket (1->1) client (n->1) source (n->1) device (n*->1) exe"
  (:require [clojure.tools.logging :as log]
            [red.device.client.source :refer [get-all-sources get-source! add-client]]
            [red.utils :refer [now]])
  (:import [red.device.client.source Source]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [clojure.lang Ref PersistentArrayMap Fn]
           [org.joda.time DateTime]
           [java.nio ByteBuffer]
           [java.util UUID]))

(defprotocol IClient
  (client->device [this data])
  (close [this]))

(deftype Client [^String             session
                 ^Source             source
                 ^PersistentArrayMap connection
                 ^Ref                device->flow
                 ^Ref                client->flow
                 ^Fn                 write-handle
                 ^Fn                 close-handle
                 ^DateTime           start-time]
  IClient
  (client->device [this data]
    (.client->device source data))

  (close [this]
    (let [{:keys [remote-addr remote-port]}  connection]
      (log/infof "close client: %s:%d" remote-addr remote-port)
      (.sub-remove source this)))

  Notify$Iface
  (Offline [this _]
    (write-handle (ByteBuffer/allocate 0))
    (close-handle))

  (MediaStarted [this _ _]
    (log/info "media start: " this))

  (MediaFinish [this _ _]
    (write-handle (ByteBuffer/allocate 0))
    (close-handle))

  (MediaData [this {:keys [^ByteBuffer payload type] :as data} _ _]
    (let [header (doto (ByteBuffer/allocate 5)
                   (.put (byte type))
                   (.putInt (.limit payload))
                   (.flip))]
      (write-handle header)
      (write-handle payload)))

  Object
  (toString [_]
    (let [{:keys [remote-addr remote-port]}  connection]
      (format "______client: %s:%d %s"
              remote-addr remote-port session))))

(defonce stream-types* {:main StreamType/Main
                        :sub  StreamType/Sub
                        :sub1 StreamType/Sub
                        :sub2 StreamType/Sub
                        :sub3 StreamType/Sub})

(defn- create-client
  "生成与客户端的相关数据"
  [source
   connection
   manufacturer account session-type info
   device->client device->close
   session-id]
  (dosync
   (let [client (Client. session-id
                         source
                         connection
                         (ref 0)
                         (ref 0)
                         device->client
                         device->close
                         (now))]
     (add-client source client)
     client)))

(defn get-all-clients []
  (dosync
   (reduce (fn [c clients]
             (clojure.set/union c (deref (deref clients))))
           #{} (get-all-sources))))

(defn get-client-by-id [^String id]
  (dosync
   ;; (some #(= id
   ;;           (get-in [:session-id] (deref %)))
   ;;       (get-all-clients))
   ))

(defn client->data [^Client client byte-buffer])

(defn close-session! [^Client client]
  (.close client))

(defn open-session!
  "处理请求,建立与设备的准备数据;返回数据发送函数和关闭函数"
  [connection {:keys [manufacturer addr port user password
                      session-type channel-id stream-type start-time end-time
                      session-id]}
   write-handle close-handle]
  (dosync
   (let [{:keys [remote-addr remote-port]} connection]
     (log/infof "creat client: %s:%d" remote-addr remote-port))
   (let [connect-type ConnectType/Tcp
         stream-type  (get stream-types* stream-type StreamType/Main)
         account      (LoginAccount. addr port user password)
         info         (PlayInfo. channel-id stream-type connect-type start-time end-time)]
     (-> (get-source! manufacturer account session-type info)
         (create-client connection
                        manufacturer account session-type info
                        write-handle close-handle
                        session-id)))))
