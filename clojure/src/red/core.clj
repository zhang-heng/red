(ns red.core
  (:import [java.util UUID]))

;;订阅数据
(defonce subscribes (ref #{}))

;;订阅请求
(defn subscribe [args]
  (let [session-id (UUID/randomUUID)]
    (dosync
     (alter subscribes conj (assoc args :session-id session-id)))
    session-id))



;;按变量名生成map
;;(correspond-args a b)->{:a a :b b}
(defmacro correspond-args [& args]
  (reduce (fn [c k] (assoc c (keyword k) k)) {} args))
