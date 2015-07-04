(ns red.server
  (:require [thrift-clj.core :as thrift]))

(thrift/import
 (:types    [device.sdk.media eMediaType MediaPackage])
 (:services [device.sdk.media Notify]))


(defn offline [])

(defn media-finish [])

(defn media-data [media] )

(thrift/defservice media-notify
  Notify
  (Offline [] (offline))
  (MediaFinish [] (media-finish))
  (MediaData [media] (media-data media)))

(defonce th (atom nil))

(defn start-server[]
  (reset! th (Thread.
              (fn []
                (thrift/serve-and-block!
                 (thrift/multi-threaded-server
                  media-notify 7007
                  :bind "localhost"
                  :protocol :compact)))))
  (.start @th)
  @th)

;;(start-server)
