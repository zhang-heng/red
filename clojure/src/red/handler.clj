(ns red.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]

            [red.routes.home :refer [base-routes]]
            [red.routes.media.status :refer [status-routes]]
            [red.routes.media.realplay :refer [realplay-routes]]
            [red.routes.media.playback :refer [playback-routes]]
            [red.routes.media.voicetalk :refer [voicetalk-routes]]))

(def app
  (-> (routes
       status-routes
       realplay-routes
       playback-routes
       voicetalk-routes
       base-routes)
      wrap-json-params
      wrap-json-response
      wrap-reload))
