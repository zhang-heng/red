(ns red.repl
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [red.handler :refer [app]]
            [red.client.core :refer [start-gtsp-server]])
  (:gen-class))

(defn -main []
  (run true))
