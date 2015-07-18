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
                  :write-completion (completion :write att)
                  :write-status (ref nil)
                  :read-completion (completion :read att)
                  :send-queue (ref [])))))

(defn- do-accept [connections connection attachment completion-handler]
  (let [{:keys [server receive-handler accept-handler]} attachment
        {:keys [read-completion] :as connections} (mk-attachment (assoc attachment :socket connection))]
    (prn "accept:" (.getRemoteAddress connection))
    (.accept server attachment completion-handler)
    (dosync connections conj completion-handler)
    (let [len (accept-handler connections)]
      (when (number? len)
        (let [buffer (ByteBuffer/allocate len)]
          (.read connection buffer buffer read-completion))))))

(defn- do-read [attachment-ref result buffer]
  (let [{:keys [socket read-completion receive-handler] :as connection} (deref attachment-ref)]
    (if (neg? result)
      (prn "disconnet:r " (.getRemoteAddress socket))
      (if (.hasRemaining buffer)
        (.read socket buffer buffer read-completion)
        (let [len (receive-handler attachment-ref buffer)]
          (when (number? len)
            (let [buffer (ByteBuffer/allocate len)]
              (.read socket buffer buffer read-completion))))))))

(defn- do-write [attachment-ref result buffer]
  (let [{:keys [socket write-completion] :as connection} (deref attachment-ref)]
    (prn "do-write: buffer")
    (if (neg? result)
      (prn "disconnet:w " (.getRemoteAddress socket))
      (if (.hasRemaining buffer)
        (.write socket buffer buffer write-completion)
        (dosync (let [{:keys [send-queue write-completion write-status]} connection
                      buffer (peek @send-queue)]
                  (if (empty? @send-queue)
                    (ref-set write-status :waitting)
                    (do (alter send-queue pop)
                        (send (agent nil)
                              (fn [_] (.write socket buffer buffer write-completion)))))))))))

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
  (dosync
   (let [{:keys [socket]} (deref connection)
         buff (-> (java.util.Date.) (str "\r\n") String. (.getBytes) (ByteBuffer/wrap))]
     (.write socket buff))))

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
