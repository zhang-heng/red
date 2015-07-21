(ns red.routes.media.voice-talk
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes voice-talk-routes
  (context "/voice-talk" []
           ;;启动对讲
           (POST "/" [manufacturer host port user password
                      channel-id
                      voice-type]
                 (str manufacturer host port user password channel-id voice-type))
           ;;状态
           (context "/:session-id" [session-id]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop"))))
