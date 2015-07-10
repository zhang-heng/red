(ns red.routes.media.realplay
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes realplay-routes
  (context "/realplay" []
           ;;启动实时
           (POST "/play" [manufacturer
                          host port user password
                          channel-id stream-type]
                 (str manufacturer host port user password channel-id stream-type))
           ;;状态
           (context "/:session" [session]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop"))))
