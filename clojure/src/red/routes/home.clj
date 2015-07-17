(ns red.routes.home
  (:require [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [response]]
            [compojure.route :refer [not-found]]
            [clojure.java.io :as io])
  (:import [java.util Properties]))
;;待办:添加阈值判断，通过获取带宽、cpu、通道数、设备数来拒绝请求

(defn get-version [dep]
  (or (System/getProperty "red.version")
      (let [path (format "META-INF/maven/%s/%s/pom.properties" (or (namespace dep) (name dep)) (name dep))
            props (io/resource path)]
        (when props
          (with-open [stream (io/input-stream props)]
            (let [props (doto (Properties.) (.load stream))]
              (.getProperty props "version")))))))

(defroutes base-routes
  (GET "/" [] (response {:msg "Welcome, 欢迎, いらっしゃいませ",
                         :version (str 'red "-" (get-version 'red))
                         :time (.format (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss")
                                        (java.util.Date.))}))
  (not-found "Not Found"))
