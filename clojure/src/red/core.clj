(ns red.core
  (:require [red.server :as server])
  (:import [dvr.netsdk Notify$Client]
           [org.apache.thrift.transport TSocket TMemoryBuffer]
           [org.apache.thrift.protocol TBinaryProtocol TJSONProtocol])
  (:gen-class :name demo.core :main true))

(defn -main []
  (let [transport (doto (TSocket. "localhost" 8090)
                    (.open))
        protocol (TBinaryProtocol. transport)
        client (Notify$Client. protocol)]
    (time
     (doseq [i (range 1 1000)]
       (.MediaData client "aa" dvr.netsdk.eMediaType/FileHeader (String. (byte-array (* 1024 1024))))))
    (.close transport)))

(defn lanuch []
  (let [transport (TMemoryBuffer. 1024)
        protocol (TBinaryProtocol. transport)
        client (Notify$Client. protocol)]
    (.send_TestBytes client (java.nio.ByteBuffer/wrap (byte-array (* 1024 1))))
    (let [output (byte-array (.length transport))]
      (.readAll transport output 0 (.length transport))
      (.close transport)
      (String. output))))

(time
 (doseq [i (range 1 1000)]
   (lanuch)))
