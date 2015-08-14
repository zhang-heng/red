(ns red.device.client.operate)

(defprotocol IOperate
  (can-multiplex? [this args] "能否复用")
  (close [this] "释放当前对象的操作: 1.通知所有子层offline, 2:通知上层移除本对象"))

(defn can-multiplex+?
  "由于defprotocol不能处理 option args, 故写此函数"
  [operate & args]
  (can-multiplex? operate args))
