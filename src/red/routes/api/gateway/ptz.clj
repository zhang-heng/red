(ns red.routes.api.gateway.ptz
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes gateway-ptz-routes
  (context "/ptz/:device-id" [device-id]
           (GET "/" [])
           (GET "/status" [])

           (POST "/lift" [])
           (POST "/right" [])
           (POST "/up" [])
           (POST "/down" [])))
