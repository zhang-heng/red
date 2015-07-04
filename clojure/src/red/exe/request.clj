(ns red.exe.request
  (:import [java.nio ByteBuffer])
  (:require [thrift-clj.core :as thrift]))

(thrift/import
 (:types    [device.sdk.media eMediaType MediaPackage])
 (:clients  [device.sdk.media Sdk Notify]))

(with-open [c (thrift/connect! Notify ["localhost" 7007])]
  (Notify/Offline c)
  (Notify/MediaFinish c)
  (let [mp (MediaPackage. "session" eMediaType/FileHeader 0 (String. (byte-array (* 1024 1024 10))))]
    (dotimes [n 10]
      (thrift/->thrift mp)
      (time
       (Notify/MediaData c mp)))))
