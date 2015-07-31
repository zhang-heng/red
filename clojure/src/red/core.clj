(ns red.core
  (:require [red.client.restful :refer [check-timeout-task]]))

(defn start-check-task
  "启动维护任务@返回关闭函数"
  []
  (let [running (promise)
        task    (Thread. #(while (deref running 1000 true)
                            (check-timeout-task)))]
    (.start task)
    #(try (do (deliver running false)
              (.join task)
              (.stop task))
          (catch Exception _))))
