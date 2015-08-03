(ns red.server
  (:require [clojure.tools.logging :as log]
            [red.core :refer [run stop]]
            [environ.core :refer [env]])
  (:import [org.apache.commons.daemon DaemonContext])
  (:gen-class :implements [org.apache.commons.daemon.Daemon]))

(defn -main []
  (run true))

;;;daemon
(defn -init [this, ^DaemonContext context])

(defn -start [this]
  (run false))

(defn -stop [this]
  (stop))
