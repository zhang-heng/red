(ns red.repl
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [red.handler :refer [app]]
            [red.client.core :refer [start-gtsp-server]]
            [environ.core :refer [env]])
  (:import [org.eclipse.jetty.server Server])
  (:gen-class))

(defonce ^:private server (atom nil))

(defn run [join?]
  (let [status (assoc {}
                 :gtsp (start-gtsp-server "0.0.0.0" 7748)
                 :rest (run-jetty app {:port 8080, :join? join?}))]
    (reset! server status)))

(defn -main []
  (run true))




(defn start []
  (run false))

(defn stop []
  (when-let [^Server rest (:rest @server)]
    (.stop rest)))
