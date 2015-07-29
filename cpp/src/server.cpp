#include "server.h"

#include <exception>
#include <random>
#include <thread>
#include <thrift/concurrency/ThreadManager.h>
#include <thrift/concurrency/PosixThreadFactory.h>
#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/server/TSimpleServer.h>
#include <thrift/server/TThreadPoolServer.h>
#include <thrift/server/TThreadedServer.h>
#include <thrift/transport/TServerSocket.h>
#include <thrift/transport/TTransportUtils.h>

using namespace apache::thrift;
using namespace apache::thrift::concurrency;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace apache::thrift::server;
using boost::shared_ptr;

using namespace device::netsdk;

class TServerEventHandlerImpl :public TServerEventHandler{
public:
  TServerEventHandlerImpl(std::function<void()> f) : _f(f){}
  virtual void preServe() {_f();}
private:
  std::function<void()> _f;
};

void Server::ServerStarted(){
  std::cout<<"sdk started, tell clojure ..."<<std::endl;
  client = new Client(_client_port);
  client->send_lanuched();
}

Server::Server(int client_port) {
  _client_port = client_port;
  shared_ptr<Server> handler(new Server());
  shared_ptr<TProcessor> processor(new SdkProcessor(handler));
  shared_ptr<TTransportFactory> transportFactory(new TBufferedTransportFactory());
  shared_ptr<TProtocolFactory> protocolFactory(new TBinaryProtocolFactory());

  shared_ptr<TServerEventHandler> e (new TServerEventHandlerImpl([&](){ServerStarted();}));

  boost::shared_ptr<ThreadManager> threadManager = ThreadManager::newSimpleThreadManager(workerCount);
  boost::shared_ptr<PosixThreadFactory> threadFactory = boost::shared_ptr<PosixThreadFactory>(new PosixThreadFactory());
  threadManager->threadFactory(threadFactory);
  threadManager->start();

  while(true){
    try{
      _listen_port = GetRandomPort(10000, 65535);
      shared_ptr<TServerTransport> serverTransport(new TServerSocket(_listen_port));
      TThreadPoolServer server(processor, serverTransport, transportFactory, protocolFactory, threadManager);
      server.setServerEventHandler(e);
      server.serve();
      return;
    }
    catch(std::exception e){
      std::cout<<e.what()<<std::endl;
    }
    std::this_thread::sleep_for(std::chrono::milliseconds(100));
  }
}

Server::~Server(){
  _devices.clear();
  _sources.clear();
}

int Server::GetRandomPort(int from, int to){
  std::random_device rd;
  return from + rd() % (to - from);
}

void Server::Testing(const int32_t Bps) {
  printf("Testing\n");
}
