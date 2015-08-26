(ns red.device.client
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [now pass-mill]]
            [red.device.source])
  (:import [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [red.device.source Source]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]
           [clojure.lang Ref PersistentArrayMap Fn Atom]))

(defrecord State [^String             session
                  ^Source             source
                  ^PersistentArrayMap connection
                  ^Atom               device->flow
                  ^Atom               client->flow
                  ^Fn                 write-handle
                  ^Fn                 close-handle
                  ^DateTime           start-time])

(gen-class :name "red.device.client.Client"
           :implements [device.netsdk.Sdk$Iface device.netsdk.Notify$Iface java.io.Closeable]
           :state state
           :prefix "-"
           :constructors {[red.device.client.State] []}
           :init init
           :methods [[send [java.nio.ByteBuffer] void]])

(defn -init [^State s] [[] s])

(defn -close [_])

(defn -toString [this] "")

(defn -send [this ^ByteBuffer data])

(defn -Offline [this _])

(defn -MediaStarted [this _ _])

(defn -MediaFinish [this _ _])

(defn -MediaData [this data _ _])
