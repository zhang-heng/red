(ns red.routes.media.realplay
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [red.client.restfull :refer [subscribe correspond-args]]
            [environ.core :refer [env]]))

(defroutes realplay-routes
  (context "/realplay" []
           ;;启动实时
           (POST "/" [manufacturer
                      addr port user password
                      channel-id stream-type]
                 {:session-id
                  (subscribe (assoc (correspond-args manufacturer addr port user password channel-id stream-type)
                               :session-type :realplay))
                  :gtsp-port (env :gtsp-port)})

           ;;状态
           (context "/:session-id" [session-id]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop"))))
