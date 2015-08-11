(ns red.device.sdk.launcher
  (:import [clojure.lang Fn Ref PersistentQueue]
           [java.io InputStreamReader BufferedReader File]))

(defn- ^ProcessBuilder creat-process-builder [^String path command]
  (doto (ProcessBuilder. ^"[Ljava.lang.String;" command)
    (.directory (File. path))
    (.. (environment)
        (put "LD_LIBRARY_PATH" path))))

(defn- get-proc-pid [^Process process]
  (when-let [f (.. process getClass (getDeclaredField "pid"))]
    (.setAccessible f true)
    (long (.get f process))))

(defn- check-cpu [^Proc proc])

(defn- check-mem [^Proc proc])

(defprotocol IProc
  (get-pid [this])
  (get-cpu [this])
  (get-mem [this])
  (close [this]))

(defrecord Proc [^Process proc
                 ^Long    pid
                 ^Ref     mem
                 ^Ref     cpu]
  IProc
  (get-pid [this] pid)
  (get-cpu [this] )
  (get-mem [this])
  (close [this]
    (io!
     (try
       (.destroy proc)
       (catch Exception e (prn e)))))

  Object
  (toString [_]))

(defn check-proc-status [^Proc proc]
  (dosync
   ((juxt [check-cpu check-mem]) proc)))

(defn launch!
  "启动进程"
  [printer crasher path working-path & args]
  (io!
   (try
     (let [command       (->> (map str args)
                              (apply merge [path])
                              (into-array String))
           proc-builder  (creat-process-builder working-path command)
           proc          (.start proc-builder)
           br            (-> (.getInputStream proc) InputStreamReader. BufferedReader.)
           pid           (get-proc-pid proc)]
       (future
         (while (when-let [l (.readLine br)]
                  (printer l)
                  true))
         (crasher))
       (Proc. proc pid (ref PersistentQueue/EMPTY) (ref PersistentQueue/EMPTY)))
     (catch Exception e (prn e)))))
