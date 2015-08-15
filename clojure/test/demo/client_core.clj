(ns demo.client-core
  (:require [clojure.tools.logging :as log]
            [red.device.client.sdk.core :refer [get-all-executors clean-executors]]
            [red.device.client.device :refer [get-all-devices]]
            [red.device.client.source :refer [get-all-sources]]
            [red.device.client.core :refer [open-session! close-session! get-all-clients]]
            [clojure.test :refer :all])
  (:import [java.util UUID]))

(def test-session
  {:manufacturer "dahua"
   ;;:addr         "192.168.8.85"
   :addr         "124.64.74.180"
   :port         37777
   :user         "admin"
   :password     "admin"
   :session-type :realplay
   :channel-id   1
   :session-id   (str (UUID/randomUUID))})

(defn test-write-handle [byte-buffer]
  (log/info byte-buffer))

(defn test-close-handle []
  (log/info "close"))

(def connection {:local-addr "127.0.0.1"
                 :local-port 123
                 :remote-addr "127.0.0.1"
                 :remote-port 456})

(open-session! connection test-session test-write-handle test-close-handle)

(log/info (str "exes: \n" (clojure.string/join "\n" (map str (get-all-executors)))))
(log/info (str "devices: \n" (clojure.string/join "\n" (map str (get-all-devices)))))
(log/info (str "sources: \n" (clojure.string/join "\n" (map str (get-all-sources)))))
(log/info (str "clients: \n" (clojure.string/join "\n" (map str (get-all-clients)))))

(close-session! (first (get-all-clients)))

(clean-executors)
