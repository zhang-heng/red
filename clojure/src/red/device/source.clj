(ns red.device.source
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [now pass-mill]])
  (:import [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]
           [clojure.lang Ref PersistentArrayMap Fn Atom])
  (:gen-class :implements [device.netsdk.Sdk$Iface device.netsdk.Notify$Iface java.io.Closeable]
              :state state
              :prefix "-"
              :constructors {[] []}
              :init init
              :methods [[open [] void]
                        [send [java.nio.ByteBuffer] void]
                        ]))

(defn -init [] [[] {}])

(gen-class :name "realplay"
           :implements [red.device.source]
           :state state
           :prefix "realplay-"
           :constructors {[] []}
           :init init)

(defn realplay-init [] [[] {}])

(defn realplay-open [])

(defn realplay-send [])
