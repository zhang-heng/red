(ns red.routes.api.media.realplay
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [ring.util.response :refer [response status redirect not-found]]
            [red.media-server.restful :refer [subscribe! get-session-in-subscribes get-and-remove-subscribe]]
            [red.device.client.sdk.core :refer [have-exe?]]
            [red.device.client.client :refer [stream-types*]]
            [red.utils :refer [correspond-args ?->long try-do is-ip-addr? string->uuid]]
            [red.config :refer [env]]
            [clojure.tools.logging :as log]))

(defn realplay [manufacturer addr port user password channel-id stream-type]
  (let [port        (?->long port)
        channel-id  (?->long channel-id)
        stream-type (keyword stream-type)]

    (try-do [(have-exe? manufacturer)                        (str manufacturer "not found")
             (is-ip-addr? addr)                              "the addr not valid"
             (and port (< 0 port) (< port 65535))            "The port must be valid"
             channel-id                                      "the channel-id must be a number"
             (some #(= % stream-type) (keys stream-types*)) "the stream-type not found"]
            (log/debug "request a new realplay session:" manufacturer addr port user password channel-id stream-type)
            (response {:session-id (subscribe! (assoc (correspond-args manufacturer addr port user password channel-id stream-type)
                                                 :session-type :realplay))
                       :media-port (env :gtsp-port)}))))

(defn session-status [session]
  (let [subscribe (get-session-in-subscribes session)]
    ()
    (not-found {:msg "session-id not found"})))

(defn session-drop [session]
  (let [subscribe (get-and-remove-subscribe session)]
    ()
    (not-found {:msg "session-id not found"})))

(defroutes realplay-routes
  (context "/realplay" []
           ;;启动实时
           (POST "/" [manufacturer host port user password channel-id stream-type]
                 (realplay manufacturer host port user password channel-id stream-type))

           (GET "/" [manufacturer addr port user password channel-id stream-type]
                (when (= (env :clj-env) :development)
                  (realplay manufacturer addr port user password channel-id stream-type)))

           ;;状态
           (context "/:session-id" [session-id]
                    (let [session (string->uuid session-id)]
                      (GET "/status" []
                           (session-status session))
                      (DELETE "/drop" [] "drop"
                              (session-status session))))))
