(ns red.utils
  (:require [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [clj-time.coerce :refer [to-long]]
            [clj-time.format :refer [parse unparse formatters with-zone]]
            [ring.util.response :refer [response status]])
  (:import [java.nio charset.Charset]
           [java.util UUID Date]
           [java.io StringWriter PrintWriter]))

(defn ?->long [n]
  (cond
   (integer? n) n
   (number? n)  (long n)
   (string? n)  (try (Long/parseLong n) (catch Exception e (log/debug e)))
   :else nil))

(defn now []
  (time/to-time-zone
   (time/now)
   (time/time-zone-for-id "Asia/Shanghai")))

(defn pass-mill [t]
  (- (to-long (now)) (to-long t)))

(defn zh-cn-time-str ^String [dt]
  (-> (with-zone (:date-time-no-ms formatters) (time/time-zone-for-id "Asia/Shanghai"))
      (unparse dt)))

(defmacro correspond-args
  "按变量名生成map";;(correspond-args a b)->{:a a :b b}
  [& args]
  (reduce (fn [c k] (assoc c (keyword k) k)) {} args))

(defn mk-queue-handler
  "生成队列执行函数" []
  (let [ag (agent nil)]
    (fn [f] (send ag (fn [_] (try (f) (catch Exception e (log/debug e))))))))

;;
(defn exception
  ([]
   (exception "Unknown Error."))
  ([ex]
   (exception 500 ex))
  ([code ex]
   (-> (response {:msg (if (instance? Exception ex)
                         (.getMessage ^Exception ex)
                         ex)})
       (status code))))

(defn bad-request [msg]
  (exception 403 msg))

(defn conflict [msg]
  (exception 409 msg))

(defn done []
  (response {:msg "Done"}))

(defn scheduled []
  (response {:msg "scheduled"}))

(defn not-found []
  (ring.util.response/not-found {:msg "Resource not found"}))

(defn return [v]
  (if v
    (response v)
    (not-found)))

(defmacro try-do [pred-bindings & actions]
  (let [clauses (->> (partition 2 pred-bindings)
                     (map #(list (list 'not (first %)) (second %)))
                     (apply concat))]
    `(cond
      ~@clauses
      :else (try
              ~@actions
              (catch Exception ex#
                (log/warn ex#)
                (.printStackTrace ex#)
                (exception ex#))))))

(defn is-ip-addr? [addr]
  (if-let [ret (re-find #"^(\d+).(\d+).(\d+).(\d+)$" addr)]
    (every? #(and (<= 0 %) (<= % 255)) (->> (drop 1 ret) (map ?->long)))
    false))

(defn string->uuid [string]
  (try
    (UUID/fromString string)
    (catch Exception _ nil)))

(defn buffer->string [byte-buffer]
  (try
    (-> (Charset/forName "ASCII")
        (.decode byte-buffer)
        str)
    (catch Exception _ nil)))

(defn stack-trace [^Throwable e]
  (let [sw (StringWriter.)
        pw (PrintWriter. sw)]
    (.printStackTrace e pw)
    (.toString sw)))
