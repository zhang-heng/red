(ns red.handler
  (:require [compojure.core :refer [defroutes routes context]]
            [compojure.handler :refer [api]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]

            [red.routes.home                 :refer [base-routes]]
            [red.routes.demo.home            :refer [demo-routes]]
            [red.routes.api.gateway.device   :refer [gateway-device-routes]]
            [red.routes.api.gateway.media    :refer [gateway-media-routes]]
            [red.routes.api.gateway.ptz      :refer [gateway-ptz-routes]]
            [red.routes.api.media.status     :refer [status-routes]]
            [red.routes.api.media.realplay   :refer [realplay-routes]]
            [red.routes.api.media.playback   :refer [playback-routes]]
            [red.routes.api.media.voice-talk :refer [voice-talk-routes]]))

(def app
  (api (-> (routes
            (context "/api/media" []
                     status-routes
                     realplay-routes
                     playback-routes
                     voice-talk-routes)
            (context "/api/gateway" []
                     gateway-device-routes
                     gateway-media-routes)
            (context "/demo" []
                     demo-routes)
            base-routes)
           wrap-json-params
           wrap-json-response
           wrap-reload)))
