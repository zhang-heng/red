(ns red.client.asynchronous-server
  (:require [red.utils :refer [correspond-args]])
  (:import [clojure.lang Ref Fn PersistentArrayMap Atom]
           [java.nio.channels AsynchronousServerSocketChannel AsynchronousSocketChannel
            CompletionHandler AsynchronousChannelGroup]
           [java.nio ByteBuffer]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]))

;;http://blog.csdn.net/chenyi8888/article/details/8086614

;; (defonce execut-pool (Executors/newFixedThreadPool 2))
;; (defonce connections (ref #{})) ;;连接记录，用于关闭服务时释放
;; (declare completion break-connect)

;; (defn- mk-attachment [info socket]
;;   (let [att (atom nil)]
;;     (reset! att (assoc info
;;                   :write-completion (completion :write att)
;;                   :write-status (ref nil)
;;                   :socket socket
;;                   :closer #(.close socket)
;;                   :send-queue (ref [])
;;                   :read-completion (completion :read att)
;;                   :user (ref nil)))))

;; (defn- do-accept [^AsynchronousSocketChannel socket
;;                   ^PersistentArrayMap info
;;                   ^CompletionHandler completion-handler]
;;   (let [{:keys [accept-handler listener]} info
;;         {:keys [read-completion] :as connection-info} (mk-attachment info socket)]
;;     (dosync (alter connections conj socket)) ;; 添加新连接至连接记录表
;;     (.accept listener listener completion-handler) ;; 继续监听新的连接
;;     (let [len (accept-handler connection-info)]    ;; 访问新连接handler
;;       (if (number? len)
;;         (let [buffer (ByteBuffer/allocate len)]
;;           (.read socket buffer buffer read-completion))
;;         (break-connect connection-info)))))

;; (defn- do-read [^Integer result
;;                 ^ByteBuffer buffer
;;                 ^PersistentArrayMap info]
;;   (let [{:keys [socket read-completion receive-handler]} info]
;;     (if (neg? result)
;;       (break-connect info)
;;       (if (.hasRemaining buffer)
;;         (.read socket buffer buffer read-completion)
;;         (do
;;           (.flip buffer)
;;           (let [len (receive-handler info buffer)]
;;             (when (number? len)
;;               (let [buffer (ByteBuffer/allocate len)]
;;                 (.read socket buffer buffer read-completion)))))))))

;; (defn- do-write [^Integer result
;;                  ^ByteBuffer buffer
;;                  ^PersistentArrayMap info]
;;   (dosync
;;    (let [{:keys [socket send-queue write-completion write-status]} info
;;          next-buffer (peek @send-queue)]
;;      (if (neg? result)
;;        (break-connect info)
;;        (if (.hasRemaining buffer)
;;          (send (agent nil)
;;                (fn [_] (.write socket buffer buffer write-completion) ))
;;          (if (empty? @send-queue)
;;            (alter write-status :waitting)
;;            (do (alter send-queue pop)
;;                (send (agent nil)
;;                      (fn [_] (.write socket next-buffer next-buffer write-completion))))))))))

;; (defn- completion
;;   "处理完成请求
;;   phase:          accept; read; write
;;   attachment-ref  info  ; atom; atom"
;;   [phase info]
;;   (proxy [CompletionHandler] []
;;     (completed [result attachment]
;;       ":accept:     AsynchronousSocketChannel socket, AsynchronousServerSocketChannel server
;;        :read/write: Integer i, ByteBuffer buf"
;;       (try
;;         (case phase
;;           :accept (do-accept result info this)
;;           :read   (do-read  result attachment @info)
;;           :write  (do-write result attachment @info))
;;         (catch Exception e (prn phase ": " e))))

;;     (failed [e attachment]
;;       ":accept:     Throwable  e, AsynchronousServerSocketChannel server
;;        :read/write: Throwable  e, ByteBuffer buf"
;;       (prn "completion failed:" e))))

;; (defn- mk-stop-fn [server]
;;   (fn []
;;     (.close server)
;;     (doseq [connection (deref connections)]
;;       (.close connection))
;;     (dosync (ref-set connections #{}))))


;; (defn write-to
;;   "将数据写入队列"
;;   [connection buffer]
;;   (dosync
;;    (let [{:keys [socket write-status send-queue write-completion]} connection]
;;      (if (= @write-status :writing)
;;        (do (alter send-queue conj buffer))
;;        (do (ref-set write-status :writing)
;;            (send (agent nil) (fn [_] (.write socket buffer buffer write-completion))))))))

;; (defn break-connect
;;   "断开连接"
;;   [info]
;;   (let [{^AsynchronousSocketChannel socket :socket} info]
;;     (.close socket)
;;     (dosync (alter connections disj socket))))

;; (defn get-connections
;;   "获取所有客户端连接" []
;;   (dosync @connections))

;; (defn run-server
;;   [host port
;;    accept-handler receive-handler]
;;   "启动服务,
;;   receive-handler:
;;   accept-handler:"
;;   (let [listener (-> execut-pool
;;                      AsynchronousChannelGroup/withThreadPool
;;                      AsynchronousServerSocketChannel/open
;;                      (.bind (InetSocketAddress. host port)))
;;         info (correspond-args listener accept-handler receive-handler)]
;;     (.accept listener listener (completion :accept info))
;;     (mk-stop-fn listener)))



(defn run-server [])
(defn read-from [connection buffer finish-handler])
(defn write-to [connection buffer])
