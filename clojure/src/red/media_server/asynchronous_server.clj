(ns red.media-server.asynchronous-server
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [correspond-args stack-trace mk-queue-handler]])
  (:import [clojure.lang Ref Fn PersistentArrayMap Atom PersistentVector PersistentQueue Agent]
           [java.nio.channels AsynchronousServerSocketChannel AsynchronousSocketChannel
            CompletionHandler AsynchronousChannelGroup]
           [java.nio ByteBuffer]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]))

;;http://blog.csdn.net/chenyi8888/article/details/8086614

(defonce execut-pool (Executors/newFixedThreadPool 2))

(defonce connections (ref #{})) ;;连接记录

(defrecord Connection [^AsynchronousSocketChannel socket            ;;socket 连接
                       ^clojure.lang.Fn           read-handler      ;;读取完成处理函数
                       ^clojure.lang.Fn           disconnect-notify ;;连接断开通知函数
                       ^java.lang.Boolean         writing?          ;;是否正在写入
                       ^PersistentQueue           write-queue       ;;写入队列
                       ^Fn                        writer
                       ^Fn                        reader
                       ^clojure.lang.Ref          user])            ;;用户数据,供调用层写状态

(declare get-socket-info)

(defn close-connection
  "关闭函数"
  [connection]
  (let [{:keys [^AsynchronousSocketChannel socket disconnect-notify]} (deref connection)
        {:keys [local-addr local-port remote-addr remote-port]}       (get-socket-info socket)]
    (dosync
     (when (get @connections connection)
       (log/infof "connection disconneted: %s:%d <- %s:%d" local-addr local-port remote-addr remote-port)
       (alter connections disj connection)
       (try (when disconnect-notify (disconnect-notify))
            (catch Exception e (log/warn "close: \n" (stack-trace e))))
       (future (when (.isOpen socket) (.close socket)))))))

(defn- mk-stop-fn
  "创建服务停止函数"
  [^AsynchronousServerSocketChannel server]
  (fn []
    (.close server)
    (dosync
     (doseq [connection (deref connections)]
       (alter connections disj connection)
       (close-connection connection)))))

(defmulti completion* (fn [t info] t))

(defmethod completion* :accept
  [_ {:keys [accept-handler] :as ^PersistentArrayMap info}]
  (proxy [CompletionHandler] []
    (completed [^AsynchronousSocketChannel       socket
                ^AsynchronousServerSocketChannel server]
      (try
        (io! (.accept server server this)) ;; 继续监听新的连接
        (dosync
         (let [connection (ref (Connection. socket nil nil false (PersistentQueue/EMPTY)
                                            (mk-queue-handler) (mk-queue-handler) (ref nil)))]
           (alter connections conj connection)
           (accept-handler connection)))
        (catch Exception e (log/warn "tcp_server accept: \n" (stack-trace e)))))

    (failed [e server]
      (log/error "tcp_server accept failed: " e))))

(defmethod completion* :read [_ ^Ref connection]
  (proxy [CompletionHandler] []
    (completed [^Integer i
                ^ByteBuffer byte-buffer]
      (try
        (dosync
         (let [{:keys [^AsynchronousSocketChannel socket read-handler reader]} (deref connection)]
           (when (.isOpen socket)
             (if (neg? i)
               (close-connection connection)
               (if (.hasRemaining byte-buffer)
                 (reader #(.read socket byte-buffer byte-buffer this))
                 (do (.flip byte-buffer)
                     (read-handler connection byte-buffer)))))))
        (catch Exception e (log/warn "tcp_server read: \n" (stack-trace e)))))
    (failed [e byte-buffer]
      (log/error "tcp_server read failed: " e)
      (close-connection connection))))

(defmethod completion* :write [_ ^Ref connection]
  (proxy [CompletionHandler] []
    (completed [^Integer i
                ^ByteBuffer byte-buffer]
      (try
        (dosync
         (let [{:keys [^AsynchronousSocketChannel socket write-queue writing? writer]} (deref connection)]
           (when (.isOpen socket)
             (if (neg? i)
               (close-connection connection)
               (if (.hasRemaining byte-buffer)
                 (writer #(.write socket byte-buffer byte-buffer this))
                 (if (empty? write-queue)
                   (alter connection update-in [:writing?] (constantly false))
                   (let [next-buffer (peek write-queue)]
                     (alter connection update-in [:write-queue] pop)
                     (writer #(.write socket next-buffer next-buffer this)))))))))
        (catch Exception e (log/info "tcp_server write: " (stack-trace e)))))
    (failed [e byte-buffer]
      (log/info "tcp_server write failed: " e)
      (close-connection connection))))

(defn set-disconnect-notify
  "订阅断线通知"
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
   (let [{:keys [^AsynchronousSocketChannel socket reader]} (deref connection)
         byte-buffer (ByteBuffer/allocate l)]
     (alter connection update-in [:read-handler] (constantly finish-handler))
     (reader #(.read socket byte-buffer byte-buffer (completion* :read connection))))))

(defn write-to
  "将ByteBuffer写入网络，正在写则存入队列"
  [^Ref connection
   ^ByteBuffer byte-buffer]
  (dosync
   (let [{:keys [writing? write-queue ^AsynchronousSocketChannel socket writer]} (deref connection)]
     (when (.isOpen socket)
       (if writing?
         (alter connection update-in [:write-queue] conj byte-buffer)
         (writer #(.write socket byte-buffer byte-buffer (completion* :write connection))))))))

(defn get-socket-info
  "通过socket对象获取地址/端口"
  [^AsynchronousSocketChannel socket]
  (try (let [^InetSocketAddress local  (.getLocalAddress socket)
             ^InetSocketAddress remote (.getRemoteAddress socket)]
         {:local-addr (.getHostString local)
          :local-port (.getPort local)
          :remote-addr (.getHostString remote)
          :remote-port (.getPort remote)})
       (catch Exception e (log/debug e) {})))

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
