(ns red.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [red.handler :refer [app]]
            [red.client.core :refer [start-gtsp-server]])
  (:import [org.apache.commons.daemon DaemonContext])
  (:gen-class :implements [org.apache.commons.daemon.Daemon]))

(defonce ^:private server (atom nil))

(defn- run [join?]
  (let [status (assoc {}
                 :rest (run-jetty app {:port 8080, :join? false})
                 :gtsp (start-gtsp-server "0.0.0.0" 7748))]
    (reset! server status)))

(defn- stop []
  (when-let [rest (:rest @server)]
    (.stop rest)))

;;;daemon

(defn -main []
  (run true))

(defn -init [this, ^DaemonContext context])

(defn -start [this]
  (run false))

(defn -stop [this]
  (stop))


;;(-start 1)
;;(-stop 1)
