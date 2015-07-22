(ns red.exe.launcher
  (:require [environ.core :refer [env]])
  (:import [java.io
            InputStreamReader BufferedReader File]))

(defn- creat-process-builder [path command]
  (doto (ProcessBuilder. ^"[Ljava.lang.String;" command)
    (.directory (File. path))
    (.. (environment)
        (put "LD_LIBRARY_PATH" path))))

(defn- mk-kill-process!
  "杀进程"
  [proc]
  (fn []
    (try
      (.destroy proc)
      (catch Exception e (prn e)))))

(defn launch!
  "启动进程
  (launch! \"C:/Windows/system32/PING.exe\" \"C:/Windows/system32\" prn \"192.168.1.1\")"
  [path working-path out-printer & args]
  (try (let [command      (->> (apply conj [path] args)
                               (into-array String))
             proc-builder (creat-process-builder working-path command)
             proc         (.start proc-builder)
             br           (-> (.getInputStream proc) InputStreamReader. BufferedReader.)]
         (future
           (while (let [r (.readLine br)]
                    (out-printer (str r))
                    r)))
         (mk-kill-process! proc))
       (catch Exception e (prn e))))
