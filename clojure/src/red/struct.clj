;;数据描述
(ns red.struct
  (:import [clojure.lang Keyword Ref Atom IPersistentMap IPersistentSet IFn PersistentQueue]
           [java.util UUID Properties Timer Date]
           [org.joda.time DateTime]))

;;设备信息
(defrecord Device [^String  manufacturer ;;厂商名称
                   ^String  host         ;;设备地址
                   ^long    port         ;;设备端口
                   ^String  user         ;;账户
                   ^String  password     ;;密码
                   ^Long    login-id])

(defrecord RealplaySource [^Ref            dvr-ref
                           ^long           channel-id
                           ^Keyword        stream-type ;; :main :sub

                           ^Long           drayd-session-id
                           ^bytes          header-data
                           ^IPersistentSet client-sessions ;;客户端列表
                           ^Date           data-time]) ;;数据接收签到

(defrecord PlaybackSource [^Ref            dvr-ref
                           ^long           channel-id
                           ^DateTime       start-time
                           ^DateTime       end-time])

(defrecord VoiceTalkSource [^Ref            dvr-ref
                            ^long           channel-id
                            ^IPersistentSet client-sessions
                            ^Keyword        voice-type])

;;通过thrift的状态描述，保存运行参数
(defrecord Thriftor [^long methods-port  ;;方法服务端口
                     ^long notify-port]) ;;通知服务端口
