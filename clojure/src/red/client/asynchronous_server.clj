(ns red.client.asynchronous-server
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [correspond-args stack-trace]])
  (:import [clojure.lang Ref Fn PersistentArrayMap Atom PersistentVector PersistentQueue]
           [java.nio.channels AsynchronousServerSocketChannel AsynchronousSocketChannel
            CompletionHandler AsynchronousChannelGroup]
           [java.nio ByteBuffer]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]))

;;http://blog.csdn.net/chenyi8888/article/details/8086614

(defonce execut-pool (Executors/newFixedThreadPool 2))

(defonce connections (ref #{})) ;;连接记录，用于关闭服务时释放

(defrecord Connection [^AsynchronousSocketChannel socket            ;;socket 连接
                       ^clojure.lang.Fn           closer            ;;关闭函数
                       ^CompletionHandler         write-completion  ;;写入完成通知
                       ^CompletionHandler         read-completion   ;;读取完成通知
                       ^clojure.lang.Fn           read-handler      ;;读取完成处理函数
                       ^clojure.lang.Fn           disconnect-notify ;;连接断开通知函数
                       ^java.lang.Boolean         writing?          ;;是否正在写入
                       ^PersistentQueue           write-queue       ;;写入队列
                       ^clojure.lang.Ref          user])            ;;用户数据,供调用层写状态

(defn- mk-close-fn [connection]
  (fn []
    (let [{:keys [^AsynchronousSocketChannel socket disconnect-notify]} (deref connection)]
      (dosync (alter connections disj socket))
      (.close socket)
      (when disconnect-notify
        (disconnect-notify)))))

(defn- mk-stop-fn [^AsynchronousServerSocketChannel server]
  (fn []
    (.close server)
    (doseq [^AsynchronousSocketChannel socket (deref connections)]
      (.close socket))
    (dosync (ref-set connections #{}))))

(defmulti completion* (fn [t info] t))

(defmethod completion* :accept
  [_ {:keys [accept-handler] :as ^PersistentArrayMap info}]
  (proxy [CompletionHandler] []
    (completed [^AsynchronousSocketChannel       socket
                ^AsynchronousServerSocketChannel server]
      (try
        (let [connection (ref nil)
              closer (mk-close-fn connection)
              write-completion (completion* :write connection)
              read-completion (completion* :read connection)]
          (.accept server server this) ;; 继续监听新的连接
          (dosync
           (alter connections conj socket) ;; 用于统一关闭
           (ref-set connection
                    (Connection. socket closer
                                 write-completion read-completion
                                 nil nil
                                 false (PersistentQueue/EMPTY)
                                 (ref nil)))
           (accept-handler connection)))
        (catch Exception e (log/info "tcp_server accept: " (stack-trace e)))))

    (failed [e server]
      (log/info "tcp_server accept failed: " e))))

(defmethod completion* :read [_ ^Ref connection]
  (proxy [CompletionHandler] []
    (completed [^Integer i
                ^ByteBuffer byte-buffer]
      (try
        (dosync
         (let [{:keys [^AsynchronousSocketChannel socket closer read-handler]} (deref connection)]
           (if (neg? i)
             (closer)
             (if (.hasRemaining byte-buffer)
               (.read socket byte-buffer byte-buffer this)
               (do (.flip byte-buffer)
                   (read-handler connection byte-buffer))))))
        (catch Exception e (log/info "tcp_server read: " (stack-trace e)))))
    (failed [e byte-buffer]
      (log/info "tcp_server read failed: " e))))

(defmethod completion* :write [_ ^Ref connection]
  (proxy [CompletionHandler] []
    (completed [^Integer i
                ^ByteBuffer byte-buffer]
      (try
        (dosync
         (let [{:keys [closer ^AsynchronousSocketChannel scoket send-queue writing?]} (deref connection)]
           (if (neg? i)
             (closer)
             (if (.hasRemaining byte-buffer)
               (send-off (agent nil)
                         (fn [_] (.write scoket byte-buffer byte-buffer this)))
               (if (empty? @send-queue)
                 (alter connection update-in [:writing?] false)
                 (let [next-buffer (peek send-queue)]
                   (alter connection update-in [:send-queue] pop)
                   (send-off (agent nil)
                             (fn [_] (.write scoket next-buffer next-buffer this)))))))))
        (catch Exception e (log/info "tcp_server write: " (stack-trace e)))))
    (failed [e byte-buffer]
      (log/info "tcp_server write failed: " e))))




(defn disconnect-notify
  "订阅掉线通知"
  [^Ref  connection
   ^Fn   disconnect-notify]
  (dosync
   (alter connection update-in [:disconnect-notify] (constantly disconnect-notify))))

(defn read-from
  "从连接读取数据"
  [^Ref  connection
   ^Long l
   ^Fn   finish-handler]
  (dosync
   (let [{:keys [read-completion ^AsynchronousSocketChannel socket]} (deref connection)
         byte-buffer (ByteBuffer/allocate l)]
     (alter connection update-in [:read-handler] (constantly finish-handler))
     (send-off (agent nil) (fn [_] (.read socket byte-buffer byte-buffer read-completion))))))

(defn write-to
  "将ByteBuffer写入网络，正在写则存入队列"
  [^Ref connection
   ^ByteBuffer byte-buffer]
  (dosync
   (let [{:keys [writing? write-queue ^AsynchronousSocketChannel scoket write-completion]} (deref connection)]
     (if writing?
       (alter connection update-in [:write-queue] conj byte-buffer)
       (send-off (agent nil) (fn [_] (.write scoket byte-buffer byte-buffer write-completion)))))))

(defn get-socket-info
  [^AsynchronousSocketChannel socket]
  (let [^InetSocketAddress local  (.getLocalAddress socket)
        ^InetSocketAddress remote (.getRemoteAddress socket)]
    {:local-addr (.getHostString local)
     :local-port (.getPort local)
     :remote-addr (.getHostString remote)
     :remote-port (.getPort remote)}))

(defn run-server
  "启动服务"
  [^String host ^long port accept-handler]
  (let [server (-> execut-pool
                   AsynchronousChannelGroup/withThreadPool
                   AsynchronousServerSocketChannel/open
                   (.bind (InetSocketAddress. host port)))
        info (correspond-args server accept-handler)]
    (.accept server server (completion* :accept info))
    (mk-stop-fn  server)))
