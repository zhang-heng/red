(ns red.device.device
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [now pass-mill]]
            [red.device.exec])
  (:import [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [red.device.exec Exec]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]
           [clojure.lang Ref PersistentArrayMap Fn Atom]))

(defrecord State [^String id
                  ^Exec   exec])

(gen-class :name "red.device.device.Device"
           :implements [device.netsdk.Sdk$Iface device.netsdk.Notify$Iface java.io.Closeable]
           :state state
           :prefix "-"
           :constructors {[red.device.device.State] []}
           :init init
           :methods [[open [] void]
                     [send [java.nio.ByteBuffer] void]])

(defn -init [] [[] {}])
