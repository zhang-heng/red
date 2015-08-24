(ns red.routes.demo.home
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [ring.util.response :refer [response status redirect not-found]]
            [red.device.client.exe :refer [get-all-executors]]))

(defroutes demo-routes
  (GET "/" []
       (->> (get-all-executors)
            (map #(str (val %)))
            (clojure.string/join "\n")
            (response))))
