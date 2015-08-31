(ns red.config
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [clj-http.client :as client]
            [nomad :refer [defconfig]]
            [environ.core :as environ])
  (:import [java.util Properties]
           [org.apache.log4j PropertyConfigurator]))

(defn- get-this-jar []
  (->> (.getPath (io/resource "sdk"))
       (re-find #":(/.+)!/")
       (last)))

(defn- get-files-in-jar [^String jar]
  (let [jf (java.util.jar.JarFile. jar)
        en (.entries jf)]
    (->> (repeatedly #(.nextElement en))
         (take-while (fn [_] (.hasMoreElements en)))
         (map str)
         (filter #(re-find #"^sdk/.+(?<!/)$" %)))))

(defn- set-file-executable [^String path]
  (let [f (clojure.java.io/file path)]
    (when (re-find #".exe$" path)
      (log/info "set executable!")
      (.setExecutable f true))))

(defn- write-jar-to-files [^String jar files]
  (let [jf (java.util.jar.JarFile. jar)]
    (doseq [^String file files]
      (log/info "extract:" file)
      (clojure.java.io/make-parents file)
      (let [je (.getJarEntry jf file)
            is (.getInputStream jf je)]
        (with-open [o (clojure.java.io/output-stream file)]
          (while (not (let [bs   (byte-array 1024)
                            read (.read is bs)]
                        (if (= read -1)
                          true
                          (.write o bs 0 read))))))
        (set-file-executable file)))))

(defn init-sdks []
  ;;(nrepl/start-server :bind "127.0.0.1" :port 44434)
  (when-not (environ/env :dev)
    (let [jar (get-this-jar)]
      (->> (get-files-in-jar jar)
           (write-jar-to-files jar)))))

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


;; (client/get "https://api.alauda.cn/v1/auth/zhangheng/profile/"
;;             {:headers {:Authorization "c98512f37b60c418264440ab787a15f500b861a1"}})
