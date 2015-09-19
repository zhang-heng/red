(ns red.routes.api.gateway.device
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [ring.util.response :refer [response status redirect not-found]]
            [red.utils :refer [correspond-args ?->long try-do is-ip-addr?]]
            [red.device.gateway :refer [open-gateway]]))

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

(defn- -open-gateway [manufacturer addr port user password]
  (let [port        (?->long port)]
    (try-do [;;(have-exe? manufacturer)                        (str manufacturer "not found")
             (is-ip-addr? addr)                              "the addr not valid"
             (and port (< 0 port) (< port 65535))            "The port must be valid"]
            (->> (open-gateway manufacturer addr port user password)
                 (array-map :device-id)
                 response))))

(defroutes gateway-device-routes
  (context "/device" []
           (GET "/" [])
           (GET "/status"[])
           (POST "/" [manufacturer addr port user password]
                 (-open-gateway manufacturer addr port user password))
           device-routes))
