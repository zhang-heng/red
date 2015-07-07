;;主动连接
(ns red.device.active
  (:require [red.exe.launcher :as launcher]
            [red.exe.callback :as callback]
            [red.exe.request :as request]))
(def path "/home/kay/project/red/dvr/gt/")
(def name "gt")

(defonce devices (ref nil))

(defn- added-device? [media-info]
  (dosync
   (->> (select-keys media-info [:addr :port])
        (get (refer devices)))))

(defn- proc-loger [args]
  (fn [c] (prn args c)))

(defonce locker (Object.))

(defn- create-exe [media-info]
  (locking locker
    (when-let [{:keys [server port info] :as cb}
               (callback/try-to-start-listen media-info)]
      (if-let [proc (launcher/launch! path name port (proc-loger media-info))]
        (assoc cb :process proc)
        (do (callback/stop-server! server)
            (prn "error"))))))
