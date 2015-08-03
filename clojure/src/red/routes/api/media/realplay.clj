(ns red.routes.api.media.realplay
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [ring.util.response :refer [response status]]
            [red.client.restful :refer [subscribe]]
            [red.utils :refer [correspond-args ?->long]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]))

(defroutes realplay-routes
  (context "/realplay" []
           ;;启动实时
           (GET "/" [manufacturer addr port user password channel-id stream-type]

                (let [port        (?->long port)
                      channel-id  (?->long channel-id)
                      stream-type (keyword stream-type)]
                  (log/info "request a new realplay session:" manufacturer addr port user password channel-id stream-type)
                  (response {:session-id
                             (subscribe (assoc (correspond-args manufacturer addr port user password channel-id stream-type)
                                          :session-type :realplay))
                             :media-port (env :gtsp-port)})))

           ;;状态
           (context "/:session-id" [session-id]
                    (GET "/status" [] "status")
                    (DELETE "/drop" [] "drop"))))
