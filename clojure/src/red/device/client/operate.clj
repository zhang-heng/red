(ns red.device.client.operate)

(defprotocol IOperate
  (can-multiplex? [this args])
  (close [this]))

(defn can-multiplex+?
  "由于defprotocol不能处理 option args, 故写此函数"
  [operate & args]
  (can-multiplex? operate args))
