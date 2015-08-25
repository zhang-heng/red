#ifndef CLIENT_h
#define CLIENT_h

#include <iostream>
#include <vector>


#include "Notify.h"

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TSocket.h>
#include <thrift/transport/TTransportUtils.h>


class Client{
 public:
  Client(int port);
  ~Client();
  void send_log           (std::string msg);
  void send_lanuched      (int port);
  void send_connected     (std::string device_id);
  void send_offline       (std::string device_id);
  void send_media_started (std::string media_id, std::string device_id);
  void send_media_finish  (std::string media_id, std::string device_id);
  void send_media_data    (const device::info::MediaPackage &data, std::string media_id, std::string device_id);
 private:
  boost::shared_ptr<apache::thrift::transport::TTransport> transport;
  device::netsdk::NotifyClient *_client;
};

#endif
