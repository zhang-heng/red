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
                  :reader (completion :read att)
                  :writer (completion :write att)
                  :send-queue (ref [])))))

(defn- do-read [len {:keys [socket reader] :as connection}]
  (let [buffer (ByteBuffer/allocate len)]
    (.read socket buffer buffer reader)))

(defn- do-write
  [{:keys [socket writer]} buffer]
  (send (agent nil)
        (fn [_] (.write socket buffer buffer writer))))

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
          :accept (let [{:keys [server receive-handler accept-handler]} attachment
                        connection (mk-attachment (assoc attachment :socket result))]
                    (.accept server attachment this)
                    (-> (accept-handler connection)
                        (do-read connection)))

          :read (let [{:keys [socket receive-handler] :as connection} (deref attachment-ref)]
                  (when (neg? result)
                    (prn "disconnet:" (.getRemoteAddress socket))
                    (-> (receive-handler attachment attachment-ref)
                        (do-read connection))))

          :write (let [{:keys [socket] :as connection} (deref attachment-ref)]
                   (when (neg? result)
                     (prn "disconnet:" (.getRemoteAddress socket))
                     (dosync
                      (let [{:keys [send-queue writer]} connection
                            buffer (peek send-queue)]
                        (alter send-queue pop)
                        (when (empty? @send-queue)
                          (alter send-queue dissoc :writer))
                        (when buffer
                          (do-write connection buffer)))))))

        (catch Exception e (prn e))))

    (failed [e attachment]
      (prn e)
      (Thread/sleep 1000)
      (.accept attachment attachment this))))

(defn- mk-stop-fn [server]
  (fn [] (.close server)))




(defn write-to
  "将数据写入队列"
  [connection buffer]
  (let [{:keys [send-queue writer]} (deref connection)]
    (dosync
     (if writer
       (alter send-queue conj buffer)
       (do (alter connection update-in [:writer]
                  (completion :write (deref connection)))
           (do-write (deref connection) buffer))))))

(defn break-connect
  "断开连接"
  [{^AsynchronousSocketChannel connection :connection}]
  (.close connection))

(defn run-server
  [host port
   receive-handler accept-handler]
  "启动服务,
  receive-handler:
  accept-handler:"
  (let [server (-> execut-pool
                   AsynchronousChannelGroup/withThreadPool
                   AsynchronousServerSocketChannel/open
                   (.bind (InetSocketAddress. host port)))
        attachment (correspond-args server accept-handler receive-handler)]
    (.accept server attachment (completion :accept nil))
    (mk-stop-fn server)))
