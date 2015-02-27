(ns red.server
  (:import [tutorial.call CallTest$Iface CallTest$Processor]
           [org.apache.thrift.server TServer]
           [org.apache.thrift.server TServer$Args]
           [org.apache.thrift.server TSimpleServer]
           [org.apache.thrift.server TThreadPoolServer]
           [org.apache.thrift.transport TServerSocket]
           [org.apache.thrift.transport TServerTransport]))

(defn call-handler []
  (reify
    CallTest$Iface
    (Call [this request] (str request))))


(defonce sv (atom nil))

(defn stop-server []
  (when @sv
    (swap! sv
           (fn [s] (.stop s) nil))))

(defn start-server []
  (let [handler (call-handler)
        processor (CallTest$Processor. handler)]
    (future (let [server-transport (TServerSocket. 9090)
                  server (TSimpleServer.
                          (.processor (TServer$Args. server-transport) processor))]
              (stop-server)
              (swap! sv
                     (fn [_] server))
              (prn "server start")
              (.serve server)))))

;;(start-server)
;;(stop-server)
