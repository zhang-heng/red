(ns red.exe.launcher
  (:require [environ.core :refer [env]])
  (:import [java.io InputStreamReader BufferedReader]))

(defn kill-exe-by-PID [pid]
  (prn pid))

(defn clean-last-exe [manufacturer]
  (let [cmd "ps -C java"
        ps  (.. (Runtime/getRuntime)
                (exec cmd))
        br  (-> (.getInputStream ps)
                (InputStreamReader.)
                (BufferedReader.))]
    (.readLine br)
    (doseq [proc (->> (repeatedly #(.readLine br))
                      (take-while not-empty))]
      (let [pid (first (re-seq #"\d+" proc))]
        (kill-exe-by-PID pid)))))

(defn launch [manufacturer local-port]
  (clean-last-exe manufacturer)
  (let [exe-dir      (format "%s/%s" (env :exe-dir) manufacturer)
        cmd          (into-array String [(format "%s/%s.dvr" exe-dir manufacturer) local-port])

        proc-builder (doto (ProcessBuilder. ^"[Ljava.lang.String;" cmd)
                       (.directory (File. drayd-dir))
                       (.redirectErrorStream true)
                       (.redirectOutput (ProcessBuilder$Redirect/appendTo
                                         (if
                                             (= (clojure.string/lower-case (get-in setting/settings [:log :level])) "debug")
                                           (File. (format "%s/%s.dvr.stdout" drayd-dir manufacturer))
                                           (File. "/dev/null"))))
                       (.. (environment) (put "LD_LIBRARY_PATH" drayd-dir)))]
    (.. (Runtime/getRuntime) (exec (format "rm -f %s" fifo-path)) (waitFor))
    (.. (Runtime/getRuntime) (exec (format "mkfifo -m +rw %s" fifo-path)) (waitFor))
    (let [drayd-process (.start proc-builder)
          output-stream (.getOutputStream drayd-process)
          input-stream  (-> (FileInputStream. fifo-path) (DataInputStream.))
          execute       (make-queue-executor (format "%s output writer" manufacturer))
          drayd         (Drayd. manufacturer drayd-process input-stream output-stream execute {} 0)]
      (dosync (alter drayds assoc manufacturer drayd))
      (.start (Thread. #(try (while true
                               (handle-drayd-msg manufacturer (read<-drayd input-stream)))
                             (catch EOFException ex
                               (log/error ex)
                               (clean-crashed-drayd manufacturer))
                             (catch Exception ex
                               (log/error "Kill drayd because:" ex)
                               (.printStackTrace ex)
                               (clean-crashed-drayd manufacturer)
                               (.destroy drayd-process)))
                       (format "%s fifo reader" manufacturer))))))
