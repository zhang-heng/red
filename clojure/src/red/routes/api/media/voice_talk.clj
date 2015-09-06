(ns red.routes.api.media.voice-talk
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [red.utils :refer [correspond-args ?->long try-do is-ip-addr? string->uuid str-time->Timeinfo]]
            [red.media-server.restful :refer [subscribe! get-session-in-subscribes get-and-remove-subscribe]]
            [ring.util.response :refer [response status redirect not-found]]
            [red.config :refer [env]]
            [clojure.tools.logging :as log]))

(defn- voice-talk [manufacturer addr port user password channel-id voice-type]
  (let [port        (?->long port)
        channel-id  (?->long channel-id)]

    (try-do [(is-ip-addr? addr)                   "the addr not valid"
             (and port (< 0 port) (< port 65535)) "The port must be valid"
             channel-id                           "the channel-id must be a number"]
            (log/debug "request a new voice-talk session:" manufacturer addr port user password channel-id voice-type)
            (response {:session-id (subscribe! (assoc (correspond-args manufacturer addr port user password channel-id voice-type)
                                                 :session-type :voicetalk))
                       :media-port (env :gtsp-port)}))))


(defroutes voice-talk-routes
  (context "/voice-talk" []
           ;;启动对讲
           (POST "/" [manufacturer host port user password channel-id voice-type]
                 (voice-talk manufacturer host port user password channel-id voice-type))
           ;;状态
           (context "/:session-id" [session-id]
                    (GET "/status" [] "status")
                    (DELETE "/" [] "drop"))))
