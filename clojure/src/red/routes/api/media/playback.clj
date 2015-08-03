(ns red.routes.api.media.playback
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes playback-routes
  (context "/playback" []
           ;;启动回放
           (POST "/" [manufacturer
                      host port user password
                      channel-id
                      start-time end-time]
                 (str manufacturer host port user password channel-id start-time end-time))
           ;;回放控制
           (context "/:session-id" [session-id]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop")

                    (POST "/pause" [] "pause")
                    (POST "/resume" [] "resume")
                    (POST "/shift-speed" [speed] (str speed "shift-speed"))
                    (POST "/seek" [offset-seconds] (str offset-seconds "seek")))))
