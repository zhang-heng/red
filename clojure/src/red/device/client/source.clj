(ns red.device.client.source
  (:require [red.device.client.device :refer [add-device! get-all-devices]]
            [red.utils :refer [now]])
  (:import (clojure.lang Ref PersistentArrayMap)
           (java.util UUID)
           (org.joda.time DateTime)))


(defrecord SourceInfo [])

(defrecord Source [^Ref                clients
                   ^PersistentArrayMap subscribe
                   ^UUID               source-id
                   ^Ref                header-data
                   ^Ref                last-data-time
                   ^Ref                device->flow
                   ^clojure.lang.Fn    device->client
                   ^clojure.lang.Fn    device->close
                   ^Ref                client->flow
                   ^clojure.lang.Fn    client->device
                   ^clojure.lang.Fn    client->close
                   ^DateTime           start-time])

(declare get-all-sources)

(defn- mk-client->device
  [client->device client->flow]
  (fn [buffer]
    (dosync
     (alter client->flow + )
     (client->device buffer))))

(defn- mk-client->close
  [client->close user]
  (fn []
    (dosync
     (client->close user))))

(defn- mk-device->client
  [clients device->flow]
  (fn [buffer]
    (dosync
     (alter device->flow +)
     (doseq [{:keys [device->client]} (deref clients)]
       (device->client buffer)))))

(defn- mk-device-close
  [clients]
  (fn []
    (dosync
     (doseq [{:keys [device->close]} (deref clients)]
       (device->close)))))

(defn- create-source!
  "新建媒体源"
  [subscribe]
  (dosync
   (let [{:keys [sources client->device client->close] :as device} (add-device! subscribe)
         clients      (ref {})
         source-id    (UUID/randomUUID)
         device->flow (ref 0)
         client->flow (ref 0)
         source (Source. clients subscribe source-id
                         (ref nil) (ref now)
                         device->flow (mk-device->client clients device->flow) (mk-device-close clients)
                         client->flow (mk-client->device client->device source-id) (mk-client->close client->close source-id)
                         (now))]
     (alter sources conj source)
     source)))

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
   (reduce (fn [c {sources :sources}] (clojure.set/union c (deref sources)))
           #{} (get-all-devices))))

(defn get-source!
  "获取源,即生成执行程序并建立联系"
  [subscribe]
  (dosync
   (let [subscribe (select-keys subscribe [:manufacturer :addr :port :user :password
                                           :stream-type :channel-id :session-type
                                           :start-time :end-time])]
     (if-let [source (can-cource-multiplex?* subscribe)]
       source
       (create-source! subscribe)))))
