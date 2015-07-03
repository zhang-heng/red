(ns red.core
  (:require [thrift-clj.core :as thrift])
  (:gen-class))

(thrift/import
 (:types    [device.sdk.media eMediaType MediaPackage])
 (:services [device.sdk.media Sdk Notify]))

(thrift/defservice media-notify
  Notify
  (Offline [] ())
  (MediaFinish [] ())
  (MediaData [media] ()))

(thrift/serve-and-block!
 (thrift/multi-threaded-server
  media-notify 7007
  :bind "localhost"
  :protocol :compact))

;; (defn -main []
;;   (let [transport (doto (TSocket. "localhost" 8090)
;;                     (.open))
;;         protocol (TBinaryProtocol. transport)
;;         client (Notify$Client. protocol)]
;;     (time
;;      (doseq [i (range 1 100)]
;;        (.MediaData client "aa" dvr.netsdk.eMediaType/FileHeader (String. (byte-array (* 1024 1024))))))
;;     (.close transport)))
