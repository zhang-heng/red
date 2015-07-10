(ns red.routes.media.realplay
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [red.core :refer [subscribe correspond-args]]
            [environ.core :refer [env]]))

(defroutes realplay-routes
  (context "/realplay" []
           ;;启动实时
           (POST "/play" [manufacturer
                          addr port user password
                          channel-id stream-type]
                 {:session-id
                  (subscribe (assoc (correspond-args manufacturer addr port user password channel-id stream-type)
                               :session-type :realplay))
                  :gtsp-port (env :gtsp-port)})
           ;;状态
           (context "/:session" [session]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop"))))
