(ns red.device.client.operate)

(defprotocol IOperate
  (can-multiplex? [this args])
  (sub-remove [this device])
  (close [this])
  (is-you? [this device-id])
  (exe-log [this msg]))

(defn can-multiplex+? [operate & args]
  (can-multiplex? operate args))
