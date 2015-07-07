(ns red.exe.core
  (:require [red.exe.launcher :as launcher]
            [red.exe.callback :as callback]
            [red.exe.request :as request]))

(defonce procs (ref nil))

(defn find-device [device]
  (dosync
   (->> (select-keys device [:addr :port])
        (get (refer procs)))))
