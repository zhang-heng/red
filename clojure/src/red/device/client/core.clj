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

(deftype Client [^UUID     session
                 ^Source   source
                 ^Ref      device->flow
                 ^Ref      client->flow
                 ^Fn       write-handle
                 ^Fn       close-handle
                 ^DateTime start-time]
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
    (format "______client: %s"
            session)))

(defonce stream-types* {:main StreamType/Main
                        :sub  StreamType/Sub
                        :sub1 StreamType/Sub
                        :sub2 StreamType/Sub
                        :sub3 StreamType/Sub})

(defn- create-client
  "生成与客户端的相关数据"
  [source
   manufacturer account session-type info
   device->client device->close
   session-id]
  (dosync
   (log/info "creat client")
   (let [client (Client. session-id
                         source
                         (ref 0)
                         (ref 0)
                         device->client
                         device->close
                         (now))]
     (add-client source client)
     client)))

(defn get-all-clients []
  (dosync
   (reduce (fn [c {clients :clients}]
             (clojure.set/union c (vals (deref clients))))
           #{} (get-all-sources))))

(defn get-client-by-id [^UUID id]
  (dosync
   (some #(= id
             (get-in [:session-id] (deref %)))
         (get-all-clients))))

(defn get-client-by-id+ [^UUID id]
  (dosync
   (-> (get-client-by-id id))))


(defn dissoc-client! [^Client client])

(defn client->data [^Client client byte-buffer])

(defn open-session!
  "处理请求,建立与设备的准备数据;返回数据发送函数和关闭函数"
  [{:keys [manufacturer addr port user password
           session-type channel-id stream-type start-time end-time
           session-id]}
   write-handle close-handle]
  (dosync
   (let [stream-type (get stream-types* stream-type StreamType/Main)
         connect-type ConnectType/Tcp
         account (LoginAccount. addr port user password)
         info    (PlayInfo. channel-id stream-type connect-type start-time end-time)]
     (-> (get-source! manufacturer account session-type info)
         (create-client manufacturer account session-type info
                        write-handle close-handle
                        session-id)))))

(def test-session
  {:manufacturer "dahua"
   :addr         "192.168.8.85"
   :port         37777
   :user         "admin"
   :password     "admin"
   :session-type :realplay
   :channel-id   1
   :session-id (UUID/randomUUID)})

(defn test-write-handle [byte-buffer]
  (log/info byte-buffer))

(defn test-close-handle []
  (log/info "close"))

(str (open-session! test-session test-write-handle test-close-handle))

(log/info (str "exes: \n" (clojure.string/join "\n" (map str (red.device.sdk.core/get-all-executors)))))

;;(red.device.sdk.core/clean)
