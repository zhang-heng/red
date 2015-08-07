(ns red.transobj)

;;executor
;;device
;;source
;;client

(defprotocol IClient
  (client->flow   [this]
    (dosync (deref (:client->flow this))))
  (client->login  [this device-id])
  (client->logout [this device-id])
  (client->data   [this byte-buffer & id])
  (client->close  [this & id]))

(defprotocol IDevice
  (device->flow         [this]
    (dosync (:device->flow this)))
  (device->lanuched     [this])
  (device->connected    [this device-id])
  (device->offline      [this device-id])
  (device->media-data   [this media-type byte-buffer & id])
  (device->media-finish [this & id])
  (device->close        [this]))
