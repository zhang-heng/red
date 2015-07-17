(ns red.client.core
  (:import [java.nio.channels AsynchronousServerSocketChannel CompletionHandler AsynchronousChannelGroup]
           [java.nio ByteBuffer charset.Charset]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]))

(defonce server (atom nil))



(def decoder (.. (Charset/forName "GBK") newDecoder))

(defn stream [socket]
  (proxy [CompletionHandler] []
    (completed [i buf]
      (if (neg? i)
        (try
          (prn "disconnet:" (.getRemoteAddress socket))
          (catch Exception e (prn e)))
        (try
          (do
            (.flip buf)
            (prn (.getRemoteAddress socket) ": " (.decode decoder buf))
            (.compact buf)
            (.read socket buf buf this)
            )
          (catch Exception e (prn e)))))

    (cancelled [attachment]
      (prn "cancelled"))
    (failed [e buf]
      (prn e))))

(defn handler []
  (proxy [CompletionHandler] []
    (completed [socket attachment]
      (try
        (do
          (.accept attachment attachment this)
          (prn "connect:" (.getRemoteAddress socket))
          (let [buff (ByteBuffer/allocate 10)]
            (.read socket buff buff (stream socket))))
        (catch Exception e (prn e))))

    (failed [e attachment]
      (prn e))))

(defn- run-socket-server
  "启动监听"
  [host port]
  (let [executor (Executors/newFixedThreadPool 20)
        asyncChannel-group (AsynchronousChannelGroup/withThreadPool executor)]
    (.. (AsynchronousServerSocketChannel/open asyncChannel-group)
        (bind (InetSocketAddress. host port)))))

(defn run-accepts [svr]
  (.accept svr svr (handler)))

(defn stop []
  (.close @server))

(defn start []
  (when @server
    (stop))
  (reset! server (run-socket-server "0.0.0.0" 10000))
  (run-accepts @server))

(start)
;;(stop)
