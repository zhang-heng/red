(ns red.device.source
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [now pass-mill]]
            [red.device.device])
  (:import [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [red.device.device Device]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]
           [clojure.lang Ref PersistentArrayMap Fn Atom]))

(defrecord State [^String id
                  ^Device device])

(gen-class :name "red.device.source.Source"
           :implements [device.netsdk.Sdk$Iface device.netsdk.Notify$Iface java.io.Closeable]
           :state state
           :prefix "-"
           :constructors {[red.device.source.State] []}
           :init init
           :methods [[open [] void]])

(defn -init [] [[] {}])

(gen-class :name "red.device.source.Realplay"
           :implements [red.device.source.Source]
           :state state
           :prefix "realplay-"
           :constructors {[] []}
           :init init)

(defn realplay-init [] [[] {}])

(defn realplay-open [])
