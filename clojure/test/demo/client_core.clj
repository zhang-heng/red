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

(defonce session (atom 0))

(defonce outer (agent nil))

(defn mk-test-write-handle [s]
  (fn [^ByteBuffer byte-buffer]
    (send outer (fn [_] (clojure.pprint/pprint (str s " " (.limit byte-buffer)) w)))))

(defn mk-test-close-handle [s]
  (fn [] (log/info  s " close")))

(def connection {:local-addr "127.0.0.1"
                 :local-port 123
                 :remote-addr "127.0.0.1"
                 :remote-port 456})

(defn test-start-session
  ([p] (let [s @session]
         (swap! session inc)
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
    (Thread/sleep n)
    (show-all-exes)
    (close-session! client)))

(defn test1
  "1:单独打开一路,关闭,间隔数秒关闭:
     a:日志无异常;
     b:输出正常(tail -f media.out).
     c:无进程残留"
  [] (dotimes [n 1]
       (test-open-close 2000)))

(defn test2 []
  "2:单独打开一路,瞬间关闭(当进程未启动/设备未上线)
     a:日志无异常;
     b:输出正常(tail -f media.out).
     c:无进程残留"
  [] (dotimes [n 10]
       (test-open-close 0)))

(defn test3
  "3:同1,2"
  [] (dotimes [n 10]
       (test-open-close (rand-int 1000))))

(defn test4
  "4:多次打开通一路,测试分发,并依次关闭."
  [] (dotimes [n 1]
       (let [m 50 ;;分发个数
             clients (doall (take m (repeatedly #(test-start-session))))]
         (Thread/sleep 3000)
         (show-all-clients)
         (show-all-exes)
         (doseq [client clients]
           (close-session! client)))))

(defn test5
  "5:打开不同媒体通道"
  [] (let [clients (->> (range 1 9)
                        (map #(assoc (test-session) :channel-id %))
                        (map #(test-start-session %))
                        doall)]
       (Thread/sleep 1000)
       (show-all-exes)
       (show-all-clients)
       (Thread/sleep 10000)
       (doseq [client clients]
         (close-session! client))
       (show-all-exes)))

;;(test5)
