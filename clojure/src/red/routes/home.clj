(ns red.routes.home
  (:require [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [response]]
            [compojure.route :refer [not-found]]
            [clojure.java.io :as io])
  (:import [java.util Properties]))

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
