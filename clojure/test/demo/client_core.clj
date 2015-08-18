(ns demo.client-core
  "测试用例:
  1:单独打开一路,关闭,间隔数秒关闭
  2:单独打开一路,瞬间关闭(当进程未启动)
  3:单独打开一路,随机关闭(当进程未启动/设备未上线)
  4:多次打开通一路,测试分发,并依次关闭
  5:打开不同媒体通道"
  (:require [thrift-clj.core :as thrift]
            [clojure.tools.logging :as log]
            [thrift-clj.core :as thrift]
            [red.utils :refer [now pass-mill]]
            [red.device.client.sdk.core :refer [get-all-executors clean-executors]]
            [red.device.client.device :refer [get-all-devices]]
            [red.device.client.source :refer [get-all-sources]]
            [red.device.client.core :refer [open-session! close-session! get-all-clients]]
            [clojure.test :refer :all])
  (:import [java.util UUID]
           [java.nio ByteBuffer]))

(defn test-session []
  {:manufacturer "dahua"
   :addr         "192.168.8.85"
   ;;:addr         "124.64.75.10"
   :port         37777
   :user         "admin"
   :password     "admin"
   :session-type :realplay
   :channel-id   1
   :session-id (str (UUID/randomUUID))})

(defonce w (clojure.java.io/writer "media.out"))

(def session (atom 0))
(def last-time (atom (now)))
(def session-out-count (atom (sorted-map)))

(defn print-sessions []
  (-> (reduce (fn [c p] (str c (key p) "." (val p) " ")) "" @session-out-count)
      (clojure.pprint/pprint w)))

(defn mk-test-write-handle [s]
  (fn [^ByteBuffer byte-buffer]
    (swap! session-out-count update-in [s] #(if % (+ % (.limit byte-buffer)) (.limit byte-buffer)))
    (when (< 500 (pass-mill @last-time))
      (print-sessions)
      (reset! last-time (now)))))

(defn mk-test-close-handle [s]
  (fn []
    (swap! session-out-count update-in [s] (constantly "close"))
    (print-sessions)
    (swap! session-out-count dissoc s)
    (reset! last-time (now))))

(def connection {:local-addr "127.0.0.1"
                 :local-port 123
                 :remote-addr "127.0.0.1"
                 :remote-port 456})

(defn test-start-session
  ([p] (let [s @session
             connection (assoc connection :remote-port s)]
         (swap! session inc)
         (log/info "start session" s)
         (open-session! connection p
                        (mk-test-write-handle s) (mk-test-close-handle s))))
  ([] (test-start-session (test-session))))

(defn show-all-exes []
  (log/info (str "exes: \n" (clojure.string/join "\n" (map str (get-all-executors))))))

(defn show-all-devices []
  (log/info (str "devices: \n" (clojure.string/join "\n" (map str (get-all-devices))))))

(defn show-all-sources []
  (log/info (str "sources: \n" (clojure.string/join "\n" (map str (get-all-sources))))))

(defn show-all-clients []
  (log/info (str "clients: \n" (clojure.string/join "\n" (map str (get-all-clients))))))

(defn test-open-close [n]
  (let [client (test-start-session)]
    (show-all-exes)
    (Thread/sleep n)
    (close-session! client)
    (show-all-exes)))

(defn test1
  "1:单独打开一路,关闭,间隔数秒关闭:
  a:日志无异常;
  b:输出正常(tail -f media.out).
  c:无进程残留"
  [] (dotimes [n 10]
       (log/info "start test1")
       (test-open-close 3000)))

(defn test2 []
  "2:单独打开一路,瞬间关闭(当进程未启动/设备未上线)
     a:日志无异常;
     b:输出正常(tail -f media.out).
     c:无进程残留"
  [] (dotimes [n 10]
       (log/info "start test2")
       (test-open-close 0)))

(defn test3
  "3:启停综合测试;同1,2:重点考察在测试结束,系统资源中是否有进程和监听残留"
  [] (dotimes [n 100]
       (log/info "start test3")
       (test-open-close (rand-int 1000))))

(defn test4
  "4:多次打开通一路,测试分发,并依次关闭."
  [] (dotimes [n 1]
       (log/info "start test4")
       (let [m 50 ;;分发个数
             clients (doall (take m (repeatedly #(test-start-session))))]
         (show-all-clients)
         (show-all-exes)
         (Thread/sleep 3000)
         (doseq [client clients]
           (close-session! client)))))

(defn test5
  "5:打开不同媒体通道"
  [] (dotimes [n 1]
       (log/info "start test5")
       (let [clients (->> (range 0 16)
                          (map #(assoc (test-session) :channel-id %))
                          (map #(do (log/debug "-------------------------------------")
                                    (test-start-session %)))
                          doall)]
         (show-all-exes)
         (show-all-clients)
         (log/debug "@@@@@@@@@@@@@@@@@@@@@@@@@@")
         (doseq [client clients]
           (Thread/sleep 1000)
           (log/debug "-------------------------------------")
           (close-session! client))
         (show-all-exes))))
