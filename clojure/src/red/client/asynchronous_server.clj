(ns red.client.asynchronous-server
  (:require [red.utils :refer [correspond-args]])
  (:import [clojure.lang Ref Fn]
           [java.nio.channels AsynchronousServerSocketChannel AsynchronousSocketChannel
            CompletionHandler AsynchronousChannelGroup]
           [java.nio ByteBuffer]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]))

;;http://blog.csdn.net/chenyi8888/article/details/8086614

(defonce execut-pool (Executors/newFixedThreadPool 2))

(declare completion)

(defn- mk-attachment
  [{:keys [receive-handler accept-handler socket] :as args}]
  (let [att (atom nil)]
    (reset! att (assoc args
                  :write  (completion :write att)
                  :reader (completion :read att)
                  :send-queue (ref [])))))

(defn- do-accept [connections connection attachment socket]
  (let [{:keys [server receive-handler accept-handler]} attachment
        {:keys [reader] :as connections} (mk-attachment (assoc attachment :socket connection))]
    (prn "accept:" (.getRemoteAddress connection))
    (.accept server attachment socket)
    (dosync connections conj socket)
    (let [len (accept-handler connections)]
      (when (number? len)
        (let [buffer (ByteBuffer/allocate len)]
          (.read connection buffer buffer reader))))))

(defn- do-read [attachment-ref result buffer]
  (let [{:keys [socket reader receive-handler] :as connection} (deref attachment-ref)]
    (if (neg? result)
      (prn "disconnet:" (.getRemoteAddress socket))
      (if (.hasRemaining buffer)
        (.read socket buffer buffer reader)
        (let [len (receive-handler attachment-ref buffer)]
          (when (number? len)
            (let [buffer (ByteBuffer/allocate len)]
              (.read socket buffer buffer reader))))))))

(defn- do-write [attachment-ref result buffer]
  (let [{:keys [socket writer] :as connection} (deref attachment-ref)]
    ;; (if (neg? result)
    ;;   (prn "disconnet:" (.getRemoteAddress socket))
    ;;   (if (.hasRemaining buffer)
    ;;     (.write socket buffer buffer writer)
    ;;     (dosync (let [{:keys [send-queue writer]} connection
    ;;                   buffer (peek send-queue)]
    ;;               (alter send-queue pop)
    ;;               (when (empty? @send-queue)
    ;;                 (alter send-queue dissoc :writer))
    ;;               (when buffer
    ;;                 (send (agent nil)
    ;;                       (fn [_] (.write socket buffer buffer writer))))))))
    ))

(defn- completion
  "处理完成请求
  phase: accept; read; write"
  [phase attachment-ref]
  (proxy [CompletionHandler] []
    (completed [result attachment]
      ":read/write: Integer i, ByteBuffer buf
       :accept:     AsynchronousSocketChannel socket, AsynchronousServerSocketChannel attachment"
      (try
        (case phase
          :accept (do-accept attachment-ref result attachment this)
          :read   (do-read   attachment-ref result attachment)
          :write  (do-read   attachment-ref result attachment))
        (catch Exception e (prn e))))

    (failed [e {server :server}]
      (when-not (.isOpen server)
        (prn "server closed")))))

(defn- mk-stop-fn [server connections]
  (fn []
    (.close server)
    (doseq [connection (deref connections)]
      (.close connection))))


(defn write-to
  "将数据写入队列"
  [connection buffer]
  ;; (let [{:keys [send-queue writer]} (deref connection)]
  ;;   (dosync
  ;;    (alter send-queue conj buffer)
  ;;    (do (completion :write connection)
  ;;        (do-write (deref connection) buffer))))
  )

(defn break-connect
  "断开连接"
  [{^AsynchronousSocketChannel connection :connection}]
  (.close connection))

(defn run-server
  [host port
   accept-handler receive-handler]
  "启动服务,
  receive-handler:
  accept-handler:"
  (let [server (-> execut-pool
                   AsynchronousChannelGroup/withThreadPool
                   AsynchronousServerSocketChannel/open
                   (.bind (InetSocketAddress. host port)))
        attachment (correspond-args server accept-handler receive-handler)
        connections (ref #{})]
    (.accept server attachment (completion :accept connections))
    (mk-stop-fn server connections)))
