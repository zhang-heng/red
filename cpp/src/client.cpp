#include "client.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

using namespace device::netsdk;

Client::Client(int port) : _client(nullptr){
  boost::shared_ptr<TTransport> socket(new TSocket("localhost", port));
  transport = boost::shared_ptr<TTransport>((new TBufferedTransport(socket)));
  boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
  _client = new NotifyClient(protocol);
  try{
    transport->open();
  }
  catch(TException &tx){
    transport->close();
  }
}

Client::~Client(){
  transport->close();
  if(_client) delete _client;
}

void Client::send_log(std::string msg){
  _client->Log(msg);
}

void Client::send_lanuched (int port){
  //std::cout<<"sdk->test1"<<std::endl;
  //_client->Test1(device::info::MediaPackage());
  //std::cout<<"sdk->test2"<<std::endl;
  //_client->Test2("send by sdk");
  //device::info::MediaPackage mp;
  //_client->Test3(mp);
  //std::cout<<"sdk->test3"<<mp.payload.size()<<std::endl;
  //string str;
  //_client->Test4(str);
  //std::cout<<"sdk->test4"<<str.size()<<std::endl;
  _client->Lanuched(port);
}

void Client::send_connected (std::string device_id){
  _client->Connected(device_id);
}

void Client::send_offline (std::string device_id){
  _client->Offline(device_id);
}

void Client::send_media_started (std::string media_id, std::string device_id){
  _client->MediaStarted(media_id, device_id);
}

void Client::send_media_finish (std::string media_id, std::string device_id){
  _client->MediaFinish(media_id, device_id);
}

void Client::send_media_data (const device::info::MediaPackage &data, std::string media_id, std::string device_id){
  _client->MediaData(data, media_id, device_id);
}
