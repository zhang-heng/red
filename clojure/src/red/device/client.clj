(ns red.device.client
  "主动连接: socket (1->1) client (n->1) source (n->1) device (n*->1) exe"
  (:require [clojure.tools.logging :as log]
            [red.device.source :refer [get-all-sources get-source! add-client remove-client source->device]]
            [red.device.operate :refer :all]
            [red.utils :refer [now pass-mill]])
  (:import [red.device.source Source]
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
    (write-handle (ByteBuffer/allocate 0) :block))

  (MediaData [this data _ _]
    (let [{:keys [^device.types.MediaType type reserver pos block total]} (bean data)
          payload (.bufferForPayload data)
          header  (doto (ByteBuffer/allocate 17)
                    (.put (unchecked-byte (.getValue type)))
                    (.putInt reserver)
                    (.putInt pos)
                    (.putInt total)
                    (.putInt (.limit payload))
                    (.flip))]
      (swap! device->flow + (.limit payload))
      (write-handle header block)
      (write-handle (.asReadOnlyBuffer payload) block)))

  Object
  (toString [_]
    (let [{:keys [remote-addr remote-port]} connection
          pass-time (inc (pass-mill start-time))]
      (format "______client: %s, %s:%d, %d Bytes, %d ms, %s Bps"
              session
              remote-addr remote-port
              @device->flow (pass-mill start-time)
              (long (/ @device->flow (/ pass-time 1000)))))))

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

(defn client->data [^Client client ^ByteBuffer byte-buffer]
  (client->source client byte-buffer))

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
