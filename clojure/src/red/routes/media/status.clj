(ns red.routes.media.status
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes status-routes
  (GET "/status" [] ""))
