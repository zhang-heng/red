#include "client.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

using namespace device::netsdk;
 
Client::Client(int port) : client(nullptr){
  boost::shared_ptr<TTransport> socket(new TSocket("localhost", port));
  transport = boost::shared_ptr<TTransport>((new TBufferedTransport(socket)));
  boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
  client = new NotifyClient(protocol);
  try{
    transport->open();
  }
  catch(TException &tx){
    transport->close();
  }
}

Client::~Client(){
  transport->close();
  if(client) delete client;
}

void Client::send_lanuched (int port){
  client->Lanuched(port);
}

void Client::send_connected (std::string device_id){
  client->Connected(device_id);
}

void Client::send_offline (std::string device_id){
  client->Offline(device_id);
}

void Client::send_media_started (std::string media_id, std::string device_id){
  client->MediaStarted(media_id, device_id);
}

void Client::send_media_finish (std::string media_id, std::string device_id){
  client->MediaFinish(media_id, device_id);
}

void Client::send_media_data (device::info::MediaPackage data, std::string media_id, std::string device_id){
  client->MediaData(data, media_id, device_id);
}

void Client::send_test_bytes (std::string bytes){
  client->TestBytes(bytes);
}
