(ns red.routes.api.media.playback
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [ring.util.response :refer [response status redirect not-found]]
            [red.media-server.restful :refer [subscribe! get-session-in-subscribes get-and-remove-subscribe]]
            [red.device.client.client :refer [stream-types*]]
            [red.utils :refer [correspond-args ?->long try-do is-ip-addr? string->uuid str-time->Timeinfo]]
            [red.config :refer [env]]
            [clojure.tools.logging :as log]))

(defn playback [manufacturer addr port user password channel-id start-time end-time]
  (let [port        (?->long port)
        channel-id  (?->long channel-id)
        start-time  (str-time->Timeinfo start-time)
        end-time    (str-time->Timeinfo end-time)]

    (try-do [;;(have-exe? manufacturer)             (str manufacturer "not found")
             (is-ip-addr? addr)                   "the addr not valid"
             (and port (< 0 port) (< port 65535)) "The port must be valid"
             channel-id                           "the channel-id must be a number"
             start-time                           "the starttime not valid"
             end-time                             "the endtime not valid"]
            (log/debug "request a new playback session:" manufacturer addr port user password channel-id start-time end-time)
            (response {:session-id (subscribe! (assoc (correspond-args manufacturer addr port user password channel-id start-time end-time)
                                                 :session-type :playback))
                       :media-port (env :gtsp-port)}))))

(defroutes playback-routes
  (context "/playback" []
           ;;启动回放
           (POST "/" [manufacturer host port user password channel-id start-time end-time]
                 (playback manufacturer host port user password channel-id start-time end-time))
           ;;回放控制
           (context "/:session-id" [session-id]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop")

                    (POST "/pause" [] "pause")
                    (POST "/resume" [] "resume")
                    (POST "/shift-speed" [speed] (str speed "shift-speed"))
                    (POST "/seek" [offset-seconds] (str offset-seconds "seek")))))
