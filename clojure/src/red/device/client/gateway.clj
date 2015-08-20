(ns red.device.client.gateway
  "网关维护"
  (:require [clojure.tools.logging :as log]
            [red.device.client.operate :refer :all]
            [red.utils :refer [now]])
  (:import [red.device.client.device Device]
           [device.netsdk Sdk$Iface Notify$Iface]
           [device.types StreamType ConnectType]
           [device.info LoginAccount PlayInfo]
           [clojure.lang Ref PersistentArrayMap Fn Atom]
           [org.joda.time DateTime]
           [java.util UUID]))
