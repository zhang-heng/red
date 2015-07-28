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
  void send_lanuched      ();
  void send_connected     (std::string device_id);
  void send_offline       (std::string device_id);
  void send_media_started (std::string device_id, std::string media_id);
  void send_media_finish  (std::string device_id, std::string media_id);
  void send_media_data    (std::string device_id, std::string media_id, device::netsdk::MediaPackage data);
  void send_test_bytes    (std::string bytes);
 private:
  boost::shared_ptr<apache::thrift::transport::TTransport> transport;
  device::netsdk::NotifyClient *client;
};

#endif
