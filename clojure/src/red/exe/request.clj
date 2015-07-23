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
 (:types    [device.netsdk LoginAccount MediaType MediaPackage StreamType])
 (:clients  [device.netsdk Sdk]))

(defn connect [thrift-port func]
  (with-open [c (thrift/connect! Sdk ["localhost" thrift-port])]
    (func c)))

(defn init [thrift-port]
  (connect thrift-port #(Sdk/InitSDK %)))

(defn uninit [thrift-port]
  (connect thrift-port #(Sdk/CleanSDK %)))

;;ret login-id
(defn login [thrift-port addr port user pass]
  (connect thrift-port #(Sdk/Login % (LoginAccount. addr port user pass))))

(defn logout [thrift-port login-id]
  (connect thrift-port #(Sdk/Logout % login-id)))

;;实时视频
;;ret media-id
(defn realplay-start [thrift-port login-id channel stream-type]
  (let [stream-type (get {:main StreamType/Main
                          :sub StreamType/Sub}
                         stream-type StreamType/Main)])
  (connect thrift-port #(Sdk/StartRealPlay % login-id channel stream-type)))

(defn realplay-stop [thrift-port media-id]
  (connect thrift-port #(Sdk/StopRealPlay % media-id)))

;;对讲
(defn voicetalk-start [thrift-port login-id channel]
  (connect thrift-port #(Sdk/StartVoiceTalk % login-id)))

(defn voicedata-send [thrift-port talk-id data]
  (connect thrift-port #(Sdk/SendVoiceData % talk-id data)))

(defn voicetalk-stop [thrift-port talk-id]
  (connect thrift-port #(Sdk/StopVoiceTalk % talk-id)))

;;回放
(defn playback-bytime [thrift-port login-id channel start-time end-time]
  (connect thrift-port #(Sdk/PlayBackByTime % login-id channel
                                        (zh-cn-time-str start-time)
                                        (zh-cn-time-str end-time))))

(defn playback-stop [thrift-port media-id]
  (connect thrift-port #(Sdk/StopPlayBack % media-id)))

;;回放控制
(defn playback-pause [thrift-port media-id]
  (connect thrift-port #(Sdk/PlayBackPause % media-id)))

(defn playback-fast [thrift-port media-id]
  (connect thrift-port #(Sdk/PlayBackFast % media-id)))

(defn playback-slow [thrift-port media-id]
  (connect thrift-port #(Sdk/PlayBackSlow % media-id)))

(defn playBack-normalspeed [thrift-port media-id]
  (connect thrift-port #(Sdk/PlayBackNormalSpeed % media-id)))

(defn playbackseek [thrift-port media-id]
  (connect thrift-port #(Sdk/PlayBackSeek % media-id)))
