(ns red.routes.api.gateway.device
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes gateway-device-routes
  (context "/device" []
           (GET "/" [])
           ;;获取所有设备
           (GET "/status"[])
           ;;增
           (POST "/" [manufacturer addr port user password])
           ;;删
           (DELETE "/:device-id" [device-id])
           ;;查
           (GET "/:device-id" [device-id])
           ;;改
           (POST "/:device-id" [device-id manufacturer addr port user password])

           "媒体"
           (context "/:device-id/media-channel" [device-id]
                    ;;获取所有播放通道
                    (GET "/" [])
                    (GET "/status" [])
                    (context ["/:channle-id", :channel-id #"[0-9]+"] [channel-id]
                             (POST "realplay" [])
                             (POST "playback" [])))

           "对讲"
           (context "/:device-id/talk-channel" [device-id]
                    ;;获取所有播放通道
                    (GET "/" [])
                    (GET "/status" [])
                    (POST ["/:channle-id", :channel-id #"[0-9]+"] [channel-id]))))
