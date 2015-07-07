(ns red.exe.launcher
  (:require [environ.core :refer [env]])
  (:import [java.io
            InputStreamReader BufferedReader
            InputStream OutputStream FileInputStream DataInputStream
            File
            IOException EOFException]
           [java.lang ProcessBuilder$Redirect]))

(defn- creat-process-builder [path command]
  (doto (ProcessBuilder. ^"[Ljava.lang.String;" command)
    (.directory (File. path))
    (.. (environment)
        (put "LD_LIBRARY_PATH" path))))

;; 启动进程
(defn launch! [path name thrift-notify-port printer]
  (try (let [command      (into-array String [(format "%s/%s.exe" path name) (str thrift-notify-port)])
             proc-builder (creat-process-builder path command)
             proc         (.start proc-builder)
             br           (-> (.getInputStream proc) InputStreamReader. BufferedReader.)]
         (future
           (while (let [r (.readLine br)]
                    (printer (str r))
                    r)))
         proc)
       (catch Exception e (prn e))))

;; 杀进程
(defn exit-process! [{proc :process}]
  (try
    (.destroy proc)
    (catch Exception e (prn e))))
