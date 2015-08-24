(ns red.routes.api.gateway.media
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes gateway-media-routes
  (context "/realplay" []
           (GET "/" [])
           (GET "/status" [])
           (context "/:id" [id]
                    (DELETE "/" [])
                    (GET "/" [])))

  (context "/playback" []
           (GET "/" [])
           (GET "/status" [])
           (context "/:id" [id]
                    (DELETE "/" [])
                    (GET "/" [])
                    (POST "/pause" [] "pause")
                    (POST "/resume" [] "resume")
                    (POST "/shift-speed" [speed])
                    (POST "/seek" [offset-seconds])))

  (context "/voicetalk" []
           (GET "/" [])
           (GET "/status" [])
           (context "/:id" [id]
                    (DELETE "/" [])
                    (GET "/" []))))
