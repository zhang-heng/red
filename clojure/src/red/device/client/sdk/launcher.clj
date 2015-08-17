(ns red.device.client.sdk.launcher
  (:require [clojure.tools.logging :as log])
  (:import [clojure.lang Fn Ref PersistentQueue]
           [java.io InputStreamReader BufferedReader File]))

(defn- ^ProcessBuilder creat-process-builder [^String path command]
  (doto (ProcessBuilder. ^"[Ljava.lang.String;" command)
    (.directory (File. path))
    (.. (environment)
        (put "LD_LIBRARY_PATH" path))))

(defn- get-win32-pid [^Process process]
  "在windows系统下，仅获取进程句柄"
  (when-let [f (.. process getClass (getDeclaredField "handle"))]
    (.setAccessible f true)
    (.getLong f process)))

(defn- get-*nix-pid [^Process process]
  (when-let [f (.. process getClass (getDeclaredField "pid"))]
    (.setAccessible f true)
    (.getInt f process)))

(defn- get-proc-pid [^Process process]
  (let [os (.. process getClass getName)]
    (try (case os
           "java.lang.Win32Process" (get-win32-pid process)
           "java.lang.ProcessImpl" (get-win32-pid process)
           "java.lang.UNIXProcess" (get-*nix-pid process))
         (catch Exception _ -1))))

(defprotocol IProc
  (get-cpu [this])
  (get-mem [this])
  (close [this]))

(defrecord Proc [^Process proc
                 ^Long    pid
                 ^Ref     mem
                 ^Ref     cpu]
  IProc
  (get-cpu [this] )
  (get-mem [this])
  (close [this]
    (future
      (try
        (io! (.destroy proc))
        (catch Exception e (log/warn e)))))

  Object
  (toString [_]))

(defn- check-cpu [^Proc proc])

(defn- check-mem [^Proc proc])

(defn check-proc-status [^Proc proc]
  (dosync
   ((juxt [check-cpu check-mem]) proc)))

(defn launch!
  "启动进程"
  [printer crasher path working-path & args]
  (log/debugf "launch: \n path=%s \n working-path=%s \n args=%s"
              path working-path args)
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
     (catch Exception e (log/error e)))))
