(ns red.device.client.client
  "主动连接: socket (1->1) client (n->1) source (n->1) device (n*->1) exe"
  (:require [clojure.tools.logging :as log]
            [red.device.client.source :refer [get-all-sources get-source! add-client remove-client source->device]]
            [red.device.client.operate :refer :all]
            [red.utils :refer [now]])
  (:import [red.device.client.source Source]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [clojure.lang Ref PersistentArrayMap Fn Atom]
           [org.joda.time DateTime]
           [java.nio ByteBuffer]
           [java.util UUID]))

(defprotocol IClient
  (client->source [this data]))

(deftype Client [^String             session
                 ^Source             source
                 ^PersistentArrayMap connection
                 ^Atom               device->flow
                 ^Atom               client->flow
                 ^Fn                 write-handle
                 ^Fn                 close-handle
                 ^DateTime           start-time]
  IClient
  (client->source [this data]
    (source->device source data))

  IOperate
  (close [this]
    (dosync
     (let [{:keys [remote-addr remote-port]} connection]
       (log/infof "close client: %s:%d" remote-addr remote-port))
     (remove-client source session)
     (close-handle)))

  Notify$Iface
  (Offline [this _]
    (close this))

  (MediaStarted [this _ _]
    (log/info "media start: " this))

  (MediaFinish [this _ _]
    (write-handle (ByteBuffer/allocate 0)))

  (MediaData [this data _ _]
    (let [{:keys [^device.types.MediaType type reserver]} (bean data)
          payload (.bufferForPayload data)
          header  (doto (ByteBuffer/allocate 6)
                    (.put (unchecked-byte (.getValue type)))
                    (.put (unchecked-byte (bit-and 0xff reserver)))
                    (.putInt (.limit payload))
                    (.flip))]
      (write-handle header)
      (write-handle (.asReadOnlyBuffer payload))))

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
   (let [{:keys [remote-addr remote-port]} connection]
     (log/infof "creat client: %s:%d" remote-addr remote-port))
   (let [client (Client. session-id source connection
                         (atom 0) (atom 0)
                         device->client device->close
                         (now))]
     (add-client source client session-id)
     client)))

(defn get-all-clients []
  (dosync
   (reduce (fn [c pclients]
             (->> pclients val deref (conj c)))
           {} (get-all-sources))))

(defn get-client-by-id [^String id]
  (dosync
   ;; (some #(= id
   ;;           (get-in [:session-id] (deref %)))
   ;;       (get-all-clients))
   ))

(defn client->data [^Client client ^ByteBuffer byte-buffer])

(defn close-session! [^Client client]
  (close client))

(defn open-session!
  "处理请求,建立与设备的准备数据;返回数据发送函数和关闭函数"
  [connection {:keys [manufacturer addr port user password
                      session-type channel-id stream-type start-time end-time
                      session-id]}
   write-handle close-handle]
  (dosync
   (let [connect-type ConnectType/Tcp
         stream-type  (get stream-types* stream-type StreamType/Main)
         account      (LoginAccount. addr port user password)
         info         (PlayInfo. channel-id stream-type connect-type start-time end-time)]
     (-> (get-source! manufacturer account session-type info)
         (create-client connection
                        manufacturer account session-type info
                        write-handle close-handle
                        session-id)))))
