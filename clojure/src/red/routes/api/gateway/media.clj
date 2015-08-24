(ns red.routes.api.gateway.media
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes gateway-media-routes
  (context "/realplay" []
           (GET "/status" [])
           (GET "/" [])

           (context "/:id" [id]
                    ;;删
                    (DELETE "/" [])
                    ;;查
                    (GET "/" [])))

  (context "/playback" []
           (GET "/status" [])
           (GET "/" [])

           (context "/:id" [id]
                    ;;删
                    (DELETE "/" [])
                    ;;查
                    (GET "/" [])
                    ;;控
                    (POST "/pause" [] "pause")
                    (POST "/resume" [] "resume")
                    (POST "/shift-speed" [speed] (str speed "shift-speed"))
                    (POST "/seek" [offset-seconds] (str offset-seconds "seek"))))

  (context "/voicetalk" []
           (GET "/status" [])
           (GET "/" [])

           (context "/:id" [id]
                    ;;删
                    (DELETE "/" [])
                    ;;查
                    (GET "/" []))))
