(ns red.sdk.request
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
 (:types    [device.types  MediaType    StreamType]
            [device.info   LoginAccount MediaPackage])
 (:clients  [device.netsdk Sdk]))

(defn connect [thrift-port func]
  (with-open [c (thrift/connect! Sdk ["localhost" thrift-port])]
    (func c)))

(defn init [thrift-port]
  (connect thrift-port #(Sdk/InitSDK %)))

(defn uninit [thrift-port]
  (connect thrift-port #(Sdk/CleanSDK %)))

;;ret login-id
(defn login [thrift-port device-id addr port user pass]
  (connect thrift-port #(Sdk/Login % device-id (LoginAccount. addr port user pass))))

(defn logout [thrift-port device-id]
  (connect thrift-port #(Sdk/Logout % device-id)))

;;实时视频
;;ret media-id
(defn realplay-start [thrift-port device-id media-id channel stream-type]
  (let [stream-type (get {:main StreamType/Main
                          :sub StreamType/Sub}
                         stream-type StreamType/Main)])
  (connect thrift-port #(Sdk/StartRealPlay % device-id media-id channel stream-type)))

(defn realplay-stop [thrift-port device-id media-id]
  (connect thrift-port #(Sdk/StopRealPlay % device-id media-id media-id)))

;;对讲
(defn voicetalk-start [thrift-port device-id media-id channel]
  (connect thrift-port #(Sdk/StartVoiceTalk % device-id media-id)))

(defn voicedata-send [thrift-port device-id media-id data]
  (connect thrift-port #(Sdk/SendVoiceData % device-id media-id data)))

(defn voicetalk-stop [thrift-port device-id media-id]
  (connect thrift-port #(Sdk/StopVoiceTalk % device-id media-id)))

;;回放
(defn playback-bytime [thrift-port device-id media-id playback-id channel start-time end-time]
  (connect thrift-port #(Sdk/PlayBackByTime % device-id media-id channel
                                            (zh-cn-time-str start-time)
                                            (zh-cn-time-str end-time))))

(defn playback-stop [thrift-port device-id media-id]
  (connect thrift-port #(Sdk/StopPlayBack % device-id media-id)))

;;回放控制
(defn playback-pause [thrift-port device-id media-id]
  (connect thrift-port #(Sdk/PlayBackPause % device-id media-id)))

(defn playback-fast [thrift-port device-id media-id]
  (connect thrift-port #(Sdk/PlayBackFast % device-id media-id)))

(defn playback-slow [thrift-port device-id media-id]
  (connect thrift-port #(Sdk/PlayBackSlow % device-id media-id)))

(defn playBack-normalspeed [thrift-port device-id media-id]
  (connect thrift-port #(Sdk/PlayBackNormalSpeed % device-id media-id)))

(defn playbackseek [thrift-port device-id media-id]
  (connect thrift-port #(Sdk/PlayBackSeek % device-id media-id)))
