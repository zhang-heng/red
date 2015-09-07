(ns red.core
  (:require [red.config :refer [env]]
            [clojure.tools.logging :as log]
            [red.device.gateway :refer [gateway-timer-task]]
            [red.media-server.restful :refer [check-timeout-task]]
            [ring.adapter.jetty :refer [run-jetty]]
            [red.handler :refer [app]]
            [red.media-server.core :refer [start-gtsp-server]])
  (:import [org.eclipse.jetty.server Server])
  (:gen-class))

(defonce ^:private server (atom nil))

(defn- start-check-task
  "启动维护任务@返回关闭函数"[]
  (let [running (promise)
        task    (Thread. #(while (deref running 1000 true)
                            (check-timeout-task)
                            (gateway-timer-task)))]
    (.start task)
    #(try (do (deliver running false)
              (.join task)
              (.stop task))
          (catch Exception e (log/debug e)))))

(defn run [join?]
  (log/info "server start running")
  (let [status (assoc {}
                 :gtsp    (start-gtsp-server "0.0.0.0" (env :gtsp-port))
                 :checker (start-check-task)
                 :restful (run-jetty app {:port (env :rest-port), :join? join?}))]
    (reset! server status)))

(defn stop []
  (when-let [{:keys [gtsp checker restful]} (deref server)]

    (checker)
    (.stop ^Server restful)
    (gtsp)))

(defn -main []
  (run true))

;;(run false)
;;(stop)
