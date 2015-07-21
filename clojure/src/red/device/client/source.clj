(ns red.device.client.source
  (:require [red.device.client.device :refer [add-device! get-all-devices]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref PersistentArrayMap)
           (org.joda.time DateTime)))

(defrecord Source [^Ref                clients
                   ^PersistentArrayMap subscribe
                   ^Ref                header-data
                   ^Ref                last-data-time
                   ^Ref                device->flow
                   ^Ref                client->flow
                   ^clojure.lang.Fn    client->dev
                   ^clojure.lang.Fn    client->close
                   ^DateTime           start-time])

(declare get-all-sources)

(defn- mk-client->dev
  [client->dev client->flow]
  (fn [buffer]
    (dosync
     (alter client->flow +)
     (client->dev buffer))))

(defn- mk-client->close
  [client->close]
  (fn []
    (client->close)))

(defn- create-source!
  "新建媒体源"
  [subscribe]
  (dosync
   (let [{:keys [sources client->dev client->close] :as device} (add-device [subscribe])
         clients (ref #{})
         header-data (ref nil)
         last-data-time (ref (now))
         device->flow (ref 0)
         client->flow (ref 0)
         client->dev* (mk-client->dev client->dev client->flow)
         client->close* (mk-client->close clients client->close)
         source (Source. clients subscribe
                         header-data last-data-time
                         device->flow client->flow client->dev* client->close*
                         (now))]
     (alter sources conj source))))

(defn- can-cource-multiplex?*
  "源能否复用"
  [{:keys [session-type] :as subscribe}]
  (dosync
   (when (= :realplay session-type)
     (some (fn [{subscribed :subscribe :as source}]
             (when (= subscribe subscribed)
               source))
           (get-all-sources)))))

(defn get-all-sources []
  (dosync
   (reduce (fn [c {sources :sources}] (clojure.set/union c))
           #{} (get-all-devices))))

(defn get-source!
  "获取源,即生成执行程序并建立联系"
  [subscribe]
  (dosync
   (if-let [source (can-cource-multiplex?* subscribe)]
     source
     (create-source! subscribe))))
