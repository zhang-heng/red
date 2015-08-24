(ns red.routes.api.gateway.device
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes device-routes
  (context "/:device-id" [device-id]
           (DELETE "/" [])
           (GET "/" [])
           (POST "/" [device-id manufacturer addr port user password])
           (context "/media-channel" []
                    (GET "/" [])
                    (GET "/status" [])
                    (context ["/:channle-id", :channel-id #"[0-9]+"] [channel-id]
                             (POST "/realplay" [])
                             (POST "/playback" [])))
           (context "/talk-channel" []
                    (GET "/" [])
                    (GET "/status" [])
                    (context ["/:channle-id", :channel-id #"[0-9]+"] [channel-id]
                             (POST "/" [])))))

(defroutes gateway-device-routes
  (context "/device" []
           (GET "/" [])
           (GET "/status"[])
           (POST "/" [manufacturer addr port user password])
           device-routes))
