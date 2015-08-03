(ns red.utils
  (:require [clj-time.core :as time]
            [clj-time.coerce :refer [to-long]]
            [clj-time.format :refer [parse unparse formatters with-zone]]
            [ring.util.response :refer [response status]]))

(defn ?->long [n]
  (cond
   (integer? n) n
   (number? n)  (long n)
   (string? n)  (try (Long/parseLong n) (catch Exception _ nil))
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
