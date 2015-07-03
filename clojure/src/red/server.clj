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
