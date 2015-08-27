(ns red.device.exec
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [now pass-mill stack-trace]])
  (:import [device.netsdk Notify$Iface]
           [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [org.apache.thrift.server TThreadPoolServer TThreadPoolServer$Args]
           [org.apache.thrift.transport TServerSocket]
           [org.apache.thrift.protocol TBinaryProtocol]
           [clojure.lang Ref PersistentArrayMap Fn Atom PersistentQueue]
           [java.io InputStreamReader BufferedReader File]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]))

(defrecord State [^String id
                  thrift-server
                  thrift-server-port
                  exe-process])

(gen-class :name "red.device.exec.Exec"
           :implements [device.netsdk.Sdk$Iface device.netsdk.Notify$Iface java.io.Closeable]
           :state state
           :prefix "-"
           :constructors {[red.device.exec.State] []}
           :init init
           :methods [[send [java.nio.ByteBuffer] void]
                     [get_thrift_port [] long]
                     [check_proc_status [] void]])

(defn- ^ProcessBuilder creat-process-builder [^String path command]
  (doto (ProcessBuilder. ^"[Ljava.lang.String;" command)
    (.directory (File. path))
    (.. (environment)
        (put "LD_LIBRARY_PATH" path))))

(defn- get-win32-pid
  "在windows系统下，仅获取进程句柄"
  [^Process process]
  (when-let [f (.. process getClass (getDeclaredField "handle"))]
    (.setAccessible f true)
    (.getLong f process)))

(defn- get-*nix-pid
  [^Process process]
  (when-let [f (.. process getClass (getDeclaredField "pid"))]
    (.setAccessible f true)
    (.getInt f process)))

(defn- get-proc-pid
  [^Process process]
  (let [os (.. process getClass getName)]
    (try (case os
           "java.lang.Win32Process" (get-win32-pid process)
           "java.lang.ProcessImpl" (get-win32-pid process)
           "java.lang.UNIXProcess" (get-*nix-pid process))
         (catch Exception _ -1))))

(defn- launch-proc
  "启动进程"
  [printer crasher path working-path & args]
  (log/debugf "launch: \n path=%s \n working-path=%s \n args=%s"
              path working-path args)
  (io!
   (try
     (let [command       (->> (map str args) (apply merge [path]) (into-array String))
           proc-builder  (creat-process-builder working-path command)
           proc          (.start proc-builder)
           br            (-> (.getInputStream proc) InputStreamReader. BufferedReader.)
           pid           (get-proc-pid proc)]
       (future
         (while (when-let [l (.readLine br)] (printer l) true))
         (crasher))
       [proc pid (ref PersistentQueue/EMPTY) (ref PersistentQueue/EMPTY)])
     (catch Exception e (log/error e)))))

(defn- start-thrift [])

(defn -init [] [[] {}])

(defn -close []
  (future
    (try ;;(io! (thrift/stop! server))
         (catch Exception e (log/warn e))))
  (future
    (try
      ;;(io! (.destroy proc))
      (catch Exception e (log/warn e)))))
