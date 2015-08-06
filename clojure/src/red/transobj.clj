(ns red.transobj)

;;executor
;;device
;;source
;;client

(defprotocol ITransport
  (get-session-id [this])
  (get-gen-time   [this])
  (client->data   [this byte-buffer])
  (client->flow   [this])
  (client->close  [this])
  (device->data   [this byte-buffer])
  (device->flow   [this])
  (device->close  [this]))

(defprotocol IControl
  (device->connected [this])
  (device->offline [this])
  (device->media-finish [this]))


(deftype Source [^clojure.lang.Ref clients
                 ^java.util.UUID source-id
                 ^clojure.lang.Ref header-data
                 ^clojure.lang.Ref last-data-time
                 ^clojure.lang.Ref device->flow
                 ^clojure.lang.Ref client->flow
                 ^clojure.lang.PersistentArrayMap source-info
                 ^org.joda.time.DateTime start-time]
  ITransport
  (get-session-id [this] "source session")
  (get-gen-time   [this])
  (client->data   [this byte-buffer])
  (client->flow   [this])
  (client->close  [this])
  (device->data   [this byte-buffer])
  (device->flow   [this])
  (device->close  [this])

  clojure.lang.Counted
  (count [_] (count (refer clients)))

  Object
  (toString [_] (str "")))
