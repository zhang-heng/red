(ns red.client.core
  (:require [red.client.asynchronous-server :refer [run-server write-to break-connect]]
            [red.client.restfull :refer [get-and-remove-subscribe]]
            [red.device.active.realplay :refer [open-realplay]]
            [red.device.active.playback :refer [open-playback]]
            [red.device.active.voicetalk :refer [open-voicetalk]])
  (:import [java.nio ByteBuffer charset.Charset]
           [java.util.concurrent TimeUnit Executors]
           [java.net InetSocketAddress]
           [java.util UUID Date]))

(defonce server (atom nil))

(defn- uuid [byte-buffer]
  (try
    (-> (Charset/forName "ASCII")
        (.decode byte-buffer)
        (str)
        (UUID/fromString))
    (catch Exception _ nil)))

(defn- open-session
  [connection {:keys [session-type] :as subscribe}]
  (case session-type
    :realplay (open-realplay connection subscribe)
    :playback (open-playback connection subscribe)
    :voicetalk (open-voicetalk connection subscribe)))

(defn- accept-handler
  "处理新连接,返回准备接受UUID的36字长"
  [connection]
  (dosync
   (let [{user :user}  connection]
     (ref-set user :session)
     36)))

(defmulti receive-handler* (fn [connection] (deref (:user connection))))

(defmethod receive-handler* :session [{:keys [user] :as connection} buffer]
  (dosync
   (when-let [{:keys [session-id] :as subscribe}
              (-> buffer uuid get-and-remove-subscribe)]
     (open-session connection subscribe)
     (ref-set user :payload)
     4)))

(defmethod receive-handler* :header [{:keys [user] :as connection} buffer]
  (dosync
   (let [len (.getInt buffer)]
     (when (< len (* 1024 1024))
       (ref-set user :payload)
       len))))

(defmethod receive-handler* :payload [{:keys [user] :as connection} buffer]
  (dosync
   (ref-set user :header)
   2))

(defn stop []
  (@server))

(defn start []
  (when @server
    (stop))
  (reset! server (run-server "0.0.0.0" 10001
                             accept-handler receive-handler*)))

;;(start)
