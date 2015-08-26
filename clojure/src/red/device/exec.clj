(ns red.device.exec
  (:require [clojure.tools.logging :as log]
            [red.utils :refer [now pass-mill]])
  (:import [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [java.nio ByteBuffer]
           [org.joda.time DateTime]
           [clojure.lang Ref PersistentArrayMap Fn Atom]))

(defrecord State [^String id])

(gen-class :name "red.device.exec.Exec"
           :implements [device.netsdk.Sdk$Iface device.netsdk.Notify$Iface java.io.Closeable]
           :state state
           :prefix "-"
           :constructors {[red.device.exec.State] []}
           :init init
           :methods [[open [] void]
                     [send [java.nio.ByteBuffer] void]])

(defn -init [] [[] {}])
