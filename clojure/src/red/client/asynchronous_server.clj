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
(defonce connections (ref #{})) ;;连接记录，用于关闭服务时释放
(declare completion)


(defn- mk-attachment [args]
  (let [att (atom nil)]
    (reset! att (assoc args
                  :write-completion (completion :write att)
                  :write-status (ref nil)
                  :send-queue nil
                  :read-completion (completion :read att)))))

(defn- do-accept [socket info completion-handler]
  (let [{:keys [accept-handler listener]} info
        {:keys [read-completion] :as connection-info} (mk-attachment (assoc info :socket socket))]
    (prn "accept:" (.getRemoteAddress socket))
    (dosync connections conj socket)      ;; 添加新连接至连接记录表
    (.accept listener listener completion-handler) ;; 继续监听新的连接
    (let [len (accept-handler socket)] ;; 访问新连接handler
      (when (number? len)
        (let [buffer (ByteBuffer/allocate len)]
          (.read socket buffer buffer read-completion))))))

(defn- do-read [result buffer info]
  (let [{:keys [socket read-completion receive-handler]} (deref info)]
    (if (neg? result)
      (prn "disconnet:r " (.getRemoteAddress socket))
      (if (.hasRemaining buffer)
        (.read socket buffer buffer read-completion)
        (let [len (receive-handler info buffer)]
          (when (number? len)
            (let [buffer (ByteBuffer/allocate len)]
              (.read socket buffer buffer read-completion))))))))

(defn- do-write [result buffer info]
  (dosync
   (let [{:keys [socket send-queue write-completion write-status]} (deref info)
         next-buffer (peek @send-queue)]
     (prn "do-write: buffer")
     (if (neg? result)
       (prn "disconnet:w " (.getRemoteAddress socket))
       (if (.hasRemaining buffer)
         (.write socket buffer buffer write-completion)
         (if (empty? @send-queue)
           (alter info :send-queue :waitting)
           (do (alter send-queue pop)
               (send (agent nil)
                     (fn [_] (.write socket next-buffer next-buffer write-completion))))))))))

(defn- completion
  "处理完成请求
  phase:          accept; read; write
  attachment-ref  info  ;  "
  [phase attachment-ref]
  (proxy [CompletionHandler] []
    (completed [result attachment]
      ":accept:     AsynchronousSocketChannel socket, AsynchronousServerSocketChannel server
       :read/write: Integer i, ByteBuffer buf"
      (try
        (case phase
          :accept (do-accept result attachment-ref this)
          :read   (do-read result attachment attachment-ref)
          :write  (do-read result attachment attachment-ref))
        (catch Exception e (prn "" e))))

    (failed [e attachment]
      ":accept:     Throwable  e, AsynchronousServerSocketChannel server
       :read/write: Throwable  e, ByteBuffer buf"
      (prn e)
      ;; (when-not (.isOpen attachment)
      ;;   (prn "server closed"))
      )))

(defn- mk-stop-fn [server]
  (fn []
    (prn 111)
    (.close server)
    (prn 222)
    (doseq [connection (deref connections)]
      (.close connection))))


(defn write-to
  "将数据写入队列"
  [connection buffer]
  (dosync
   (let [{:keys [socket write-status send-queue]} (deref connection)]
     (if (= write-status :writing)
       (alter send-queue conj buffer)
       (send (agent nil)(fn [_] (.write socket buffer)))))))

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
  (let [listener (-> execut-pool
                     AsynchronousChannelGroup/withThreadPool
                     AsynchronousServerSocketChannel/open
                     (.bind (InetSocketAddress. host port)))
        info (correspond-args listener accept-handler receive-handler)]
    (.accept listener listener (completion :accept info))
    (mk-stop-fn listener)))
