(ns red.routes.media.voicetalk
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes voicetalk-routes
  (context "/voicetalk" []
           ;;启动对讲
           (POST "/play" [manufacturer host port user password
                          channel-id
                          voice-type]
                 (str manufacturer host port user password channel-id voice-type))
           ;;状态
           (context "/:session" [session]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop"))))
