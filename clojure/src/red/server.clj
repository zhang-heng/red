(ns red.server
  (:import [dvr.netsdk Notify$Client Notify$Iface Notify$Processor]
           [org.apache.thrift.server TServer]
           [org.apache.thrift.server TServer$Args]
           [org.apache.thrift.server TSimpleServer]
           [org.apache.thrift.protocol TBinaryProtocol TJSONProtocol]
           [org.apache.thrift.transport TServerSocket TMemoryBuffer]))

(defonce sv (atom nil))

(defn stop-server []
  (when @sv
    (swap! sv (fn [s] (.stop s) nil))))

(defn start-server []
  (let [handler (reify Notify$Iface
                  (Online      [this] (prn 111))
                  (Offline     [this])
                  (MediaData   [this session type data] ;; (prn (count data))
                    )
                  (MediaNotify [this what])
                  (AlarmNotify [this]))
        processor (Notify$Processor. handler)]
    (future (let [transport (TServerSocket. 8090)
                  server (TSimpleServer.
                          (.processor (TServer$Args. transport) processor))]
              (stop-server)
              (swap! sv
                     (fn [_] server))
              (prn "server start")
              (.serve server)))))

;;(start-server)
;;(stop-server)


(defn start-server []
  (let [input "[1,\"MediaData\",1,1,{\"1\":{\"str\":\"aa\"},\"2\":{\"i32\":1},\"3\":{\"str\":\"\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\\u0000\"}}]"

        inbuffer (doto (TMemoryBuffer. (count input))
                   (.write (.getBytes input)))
        inprotocol (TJSONProtocol. inbuffer)

        outbuffer (TMemoryBuffer. 100)
        outprotocol (TJSONProtocol. outbuffer)

        handler (reify Notify$Iface
                  (Online      [this] (prn 111))
                  (Offline     [this])
                  (MediaData   [this session type data]
                    1111)
                  (MediaNotify [this what])
                  (AlarmNotify [this])
                  (TestBytes [this list-bytes]
                    (byte-array 100)))
        processor (Notify$Processor. handler)]
    (.process processor inprotocol outprotocol)
    (let [output (byte-array (.length outbuffer))]
      (.readAll outbuffer output 0 (.length outbuffer))
      (String. output))))
