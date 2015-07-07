(ns red.exe.request
  (:import [java.nio ByteBuffer]
           [org.joda.time DateTime])
  (:require [thrift-clj.core :as thrift]
            [clj-time.core :as time]
            [clj-time.format :refer [parse unparse formatters with-zone]]))

(defn- zh-cn-time-str ^String [^DateTime dt]
  (-> (with-zone
        (:date-time-no-ms formatters)
        (time/time-zone-for-id "Asia/Shanghai"))
      (unparse dt)))

(thrift/import
 (:types    [device.sdk.media LoginAccount MediaType MediaPackage StreamType])
 (:clients  [device.sdk.media Sdk]))

(defn connect [handler func]
  (let [{port :port} handler]
    (with-open [c (thrift/connect! Sdk ["localhost" port])]
      (func c))))

(defn init [handler]
  (connect handler #(Sdk/Init %)))

(defn uninit [handler]
  (connect handler #(Sdk/Uninit %)))

;;ret login-id
(defn login [handler addr port user pass]
  (connect handler #(Sdk/Login % (LoginAccount. addr port user pass))))

(defn logout [handler login-id]
  (connect handler #(Sdk/Logout % login-id)))

;;实时视频
;;ret media-id
(defn realplay-start [handler login-id channel stream-type]
  (let [stream-type (get {:main StreamType/Main
                          :sub StreamType/Sub}
                         stream-type StreamType/Main)])
  (connect handler #(Sdk/StartRealPlay % login-id channel stream-type)))

(defn realplay-stop [handler media-id]
  (connect handler #(Sdk/StopRealPlay % media-id)))

;;对讲
(defn voicetalk-start [handler login-id channel]
  (connect handler #(Sdk/StartVoiceTalk % login-id)))

(defn voicedata-send [handler talk-id data]
  (connect handler #(Sdk/SendVoiceData % talk-id data)))

(defn voicetalk-stop [handler talk-id]
  (connect handler #(Sdk/StopVoiceTalk % talk-id)))

;;回放
(defn playback-bytime [handler login-id channel start-time end-time]
  (connect handler #(Sdk/PlayBackByTime % login-id channel
                                        (zh-cn-time-str start-time)
                                        (zh-cn-time-str end-time))))

(defn playback-stop [handler media-id]
  (connect handler #(Sdk/StopPlayBack % media-id)))

;;回放控制
(defn playback-pause [handler media-id]
  (connect handler #(Sdk/PlayBackPause % media-id)))

(defn playback-fast [handler media-id]
  (connect handler #(Sdk/PlayBackFast % media-id)))

(defn playback-slow [handler media-id]
  (connect handler #(Sdk/PlayBackSlow % media-id)))

(defn playBack-normalspeed [handler media-id]
  (connect handler #(Sdk/PlayBackNormalSpeed % media-id)))

(defn playbackseek [handler media-id]
  (connect handler #(Sdk/PlayBackSeek % media-id)))
