(ns red.routes.api.media.status
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]))

(defroutes status-routes
  (GET "/status" [] "status"))
