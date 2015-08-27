(ns red.config
  (:require [clojure.java.io :as io]
            [clojure.tools.nrepl.server :as nrepl]
            [nomad :refer [defconfig]]
            [environ.core :as environ])
  (:import [java.util Properties]
           [org.apache.log4j PropertyConfigurator]))

(defn init-sdks []
  (nrepl/start-server :bind "127.0.0.1" :port 44434)
  (when-not (environ/env :dev)
    (prn str (io/resource "dev.edn"))
    (doseq [l (file-seq (java.io.File. (str (io/resource "sdk"))))]
      (prn l))
    ))

(defn init-log []
  (let [config "log4j.properties"
        inside (doto (Properties.)
                 (.load (.. clojure.lang.RT baseLoader (getResourceAsStream config)))
                 (.setProperty "log4j.rootLogger" "INFO,console,A3"))
        outside  (str (System/getProperty "user.dir") "/" config)]
    (if (.. (io/file outside) exists)
      (PropertyConfigurator/configure outside)
      (PropertyConfigurator/configure inside))))

(def read-config
  (memoize
   #(let [outside (io/file (str (System/getProperty "user.dir") "/config"))
          insider (if (environ/env :dev)
                    (defconfig server-config (io/resource "dev.edn"))
                    (defconfig server-config (io/resource "default.edn")))]
      (if (.exists outside)
        (merge (insider) ((defconfig server-config outside)))
        (insider)))))

(defn env
  ([key] (env key nil))
  ([key not-found] (get (read-config) key not-found)))


;; (let [p (.getPath (io/resource "sdk"))]
;;   (.substring p (.indexOf p "/")))

;; (def jf (java.util.jar.JarFile. "/home/kay/project/red2/clojure/red.jar"))
;; (def je (.getJarEntry xxx "sdk/gt/gt.exe"))

;; (def is (.getInputStream jf je))

;; (def of (java.io.FileOutputStream. "gggt.exe"))
