(ns red.routes.api.media.realplay
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [ring.util.response :refer [response status redirect]]
            [red.client.restful :refer [subscribe!]]
            [red.sdk.request  :refer [*stream-types*]]
            [red.sdk.core :refer [have-exe?]]
            [red.utils :refer [correspond-args ?->long try-do]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]))

(defn realplay [manufacturer addr port user password channel-id stream-type]
  (let [port        (?->long port)
        channel-id  (?->long channel-id)
        stream-type (keyword stream-type)]
    (try-do [(have-exe? manufacturer) (str manufacturer "not found")
             (and port (< 0 port) (< port 65535)) "The port must be valid"
             channel-id "the channel-id must be a number"
             (some #(= % stream-type) (keys *stream-types*)) "the stream-type not found"]
            (log/info "request a new realplay session:" manufacturer addr port user password channel-id stream-type)
            (response {:session-id (subscribe! (assoc (correspond-args manufacturer addr port user password channel-id stream-type)
                                                 :session-type :realplay))
                       :media-port (env :gtsp-port)}))))

(defroutes realplay-routes
  (context "/realplay" []
           ;;启动实时
           (POST "/" [manufacturer addr port user password channel-id stream-type]
                 (realplay manufacturer addr port user password channel-id stream-type))

           (GET "/" [manufacturer addr port user password channel-id stream-type]
                (when (= (env :clj-env) :development)
                  (realplay manufacturer addr port user password channel-id stream-type)))

           ;;状态
           (context "/:session-id" [session-id]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop"))))
