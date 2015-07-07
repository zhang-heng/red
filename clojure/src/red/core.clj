(ns red.core
  (:import [java.nio ByteBuffer])
  (:require [thrift-clj.core :as thrift]))

;; (defn -main []
;;   (let [transport (doto (TSocket. "localhost" 8090)
;;                     (.open))
;;         protocol (TBinaryProtocol. transport)
;;         client (Notify$Client. protocol)]
;;     (time
;;      (doseq [i (range 1 100)]
;;        (.MediaData client "aa" dvr.netsdk.eMediaType/FileHeader (String. (byte-array (* 1024 1024))))))
;;     (.close transport)))
