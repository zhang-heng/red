(ns red.device.client.operate)

(defprotocol IOperate
  (can-multiplex? [this args])
  (sub-remove [this device])
  (close [this]))

(defn can-multiplex+? [operate & args]
  (can-multiplex? operate args))
