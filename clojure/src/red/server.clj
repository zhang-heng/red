(ns red.server
  (:require [red.repl :refer [run stop]]
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
