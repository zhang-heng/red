(ns red.client.core
  (:require [red.client.asynchronous-server :refer [run-server write-to]])
  (:import [java.nio.channels AsynchronousServerSocketChannel CompletionHandler AsynchronousChannelGroup]
           [java.nio ByteBuffer charset.Charset]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]))

(defonce server (atom nil))

(defn decoder [buffer]
  (.. (Charset/forName "GBK")
      newDecoder
      (decode buffer)))

(defn accept-handler [connection]
  (dosync
   (let [{user :user}  connection]
     (ref-set user :header)
     (prn "accept-handler")
     (prn connection)
     8)))

(defn receive-handler [connection buffer]
  (dosync
   (let [{user :user} connection]
     (prn (str (decoder buffer)))
     (case (deref user)
       :header
       (do
         (ref-set user :payload)
         4)
       :payload
       (do
         (ref-set user :header)
         2)))))

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

(start)
