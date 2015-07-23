(ns red.sdk.launcher
  (:import [java.io InputStreamReader BufferedReader File]))

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

(defn- get-proc-pid [process]
  (when-let [f (.. process getClass (getDeclaredField "pid"))]
    (.setAccessible f true)
    (long (.get f process))))

(defn launch!
  "启动进程
  (launch! prn #(prn \"over\") \"ping\" \"/\" \"192.168.8.1\")"
  [printer crasher path working-path & args]
  (try (let [command      (->> (map str args)
                               (apply conj [path])
                               (into-array String))
             proc-builder (creat-process-builder working-path command)
             proc         (io! (.start proc-builder))
             br           (-> (.getInputStream proc) InputStreamReader. BufferedReader.)
             pid          (get-proc-pid proc)]
         (future
           (while (when-let [r (.readLine br)]
                    (printer pid r)
                    true))
           (crasher))
         (mk-kill-process! proc))
       (catch Exception e (prn e))))
