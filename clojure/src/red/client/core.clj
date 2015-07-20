(ns red.client.core
  (:require [red.client.asynchronous-server :refer [run-server write-to]])
  (:import [java.nio.channels AsynchronousServerSocketChannel CompletionHandler AsynchronousChannelGroup]
           [java.nio ByteBuffer charset.Charset]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]))

(defonce server (atom nil))
(defonce connect (atom nil))

(defn decoder [buffer]
  (.. (Charset/forName "GBK")
      newDecoder
      (decode buffer)))

(defn accept-handler [connection]
  (prn "accept-handler")
  (reset! connect connection)
  4)

(defn receive-handler [connection buffer]
  (.flip buffer)
  (prn (str (decoder buffer)))
  8)

(defn write-handler [connection buffer]
  (write-to connection buffer))

(defn stop []
  (reset! connect nil)
  (@server))

(defn start []
  (when @server
    (stop))
  (reset! server (run-server "0.0.0.0" 10001
                             accept-handler receive-handler)))

;;(start)
