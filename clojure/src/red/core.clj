(ns red.core
  (:import [tutorial.call CallTest$Client]
           [org.apache.thrift.transport TSocket]
           [org.apache.thrift.protocol TBinaryProtocol]
           [org.apache.thrift.protocol TProtocol])
  (:gen-class :name demo.core :main true))

(defn -main []
  (let [transport (doto (TSocket. "192.168.8.12" 9090)
                    (.open))
        protocol (TBinaryProtocol. transport)
        client (CallTest$Client. protocol)
        ]
    (doseq [i (range 1 10)]
      (prn (.Call client i)))
    (.close transport)))
