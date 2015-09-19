(ns red.server
  (:require [clojure.tools.logging :as log]
            [red.core :refer [run stop]]
            [red.config :refer [init-log init-sdks]])
  (:import [org.apache.commons.daemon DaemonContext])
  (:gen-class :implements [org.apache.commons.daemon.Daemon]))

;;;jar -jar
(defn -main []
  (init-sdks)
  (init-log)
  (run true))

;;;daemon
(defn -init [this, ^DaemonContext context]
  (init-sdks)
  (init-log))

(defn -start [this]
  (run false))

(defn -stop [this]
  (stop))
