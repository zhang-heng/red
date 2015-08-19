(ns red.config
  (:require [clojure.java.io :as io]
            [nomad :refer [defconfig]]
            [environ.core :as environ])
  (:import [java.io File]
           [java.util Properties]
           [org.apache.log4j PropertyConfigurator]))

(defn init-log []
  (let [config "log4j.properties"
        inside (doto (Properties.)
                 (.load (.. clojure.lang.RT baseLoader (getResourceAsStream config)))
                 (.setProperty "log4j.rootLogger" "INFO,console,A3"))
        outside  (str (System/getProperty "user.dir") "/" config)]
    (if (.. (File. outside) exists)
      (PropertyConfigurator/configure outside)
      (PropertyConfigurator/configure inside))))

(def read-config
  (memoize
   #(let [outside (File. (str (System/getProperty "user.dir") "/config"))
          insider (if (environ/env :dev)
                    (defconfig server-config (io/resource "dev.edn"))
                    (defconfig server-config (io/resource "default.edn")))]
      (if (.exists outside)
        (merge (insider) ((defconfig server-config outside)))
        (insider)))))

(defn env
  ([key] (env key nil))
  ([key not-found] (get (read-config) key not-found)))
