#include "server.h"

#include <exception>
#include <random>
#include <thread>
#include <algorithm>

#if (defined(WIN32) || defined(WIN64))
  #include <thrift/concurrency/StdThreadFactory.h>
#else
  #include <thrift/concurrency/PosixThreadFactory.h>
#endif

#include <thrift/concurrency/ThreadManager.h>
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

//TServerEventHandlerImpl**********
class TServerEventHandlerImpl :public TServerEventHandler{
public:
	TServerEventHandlerImpl(std::function<void()> f) : _f(f){}
	virtual void preServe() {_f();}
private:
	std::function<void()> _f;
};


//Server**********
void Server::ServerStarted(){
	std::cout<<"sdk thrift server started, tell clojure sdk port is:" <<  _listen_port <<std::endl;
	try{
		client = new Client(_client_port);
		client->send_lanuched(_listen_port);
		client->send_connected("abc");
	}
	catch (std::exception e){
		std::cerr << e.what() << ", can not connect back!" << std::endl;
		exit(0);
	}
}

Server::Server(int client_port) {
	_client_port = client_port;
	shared_ptr<Server> handler(new Server());
	shared_ptr<TProcessor> processor(new SdkProcessor(handler));
	shared_ptr<TTransportFactory> transportFactory(new TBufferedTransportFactory());
	shared_ptr<TProtocolFactory> protocolFactory(new TBinaryProtocolFactory());

	shared_ptr<TServerEventHandler> e (new TServerEventHandlerImpl([&](){ServerStarted();}));

	boost::shared_ptr<ThreadManager> threadManager = ThreadManager::newSimpleThreadManager(workerCount);
#if (defined(WIN32) || defined(WIN64))
	boost::shared_ptr<StdThreadFactory> threadFactory = boost::shared_ptr<StdThreadFactory>(new StdThreadFactory());
#else
	boost::shared_ptr<PosixThreadFactory> threadFactory = boost::shared_ptr<PosixThreadFactory>(new PosixThreadFactory());
#endif
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
			std::cout << e.what() << std::endl;
		}
		std::this_thread::sleep_for(std::chrono::milliseconds(100));
	}
}

Server::~Server(){
}

int Server::GetRandomPort(int from, int to){
	std::random_device rd;
	return from + rd() % (to - from);
}

Device* Server::FindDevice(std::string id){
	auto it = _devices.find(id);
	if(it == _devices.end())
		return nullptr;
	return it->second;
}

bool Server::Login(const std::string& device_id, const  ::device::info::LoginAccount& account){
	auto device = FindDevice(device_id);
	if(device){
		return device->Login(account);
	}else{
		device = new Device(device_id, account);
		_devices.insert(std::pair<std::string, Device*>(device_id, device));
		return device->Login();
	}
}

bool Server::Logout(const std::string& device_id) {
	auto device = FindDevice(device_id);
	if(device)
		return device->Logout();
	else{
		device::info::InvalidOperation io;
		io.what = 0;
		io.why = "device_id not found";
		throw io;
	}
}

bool Server::StartRealPlay(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id){
	auto device = FindDevice(device_id);
	if(device)
		return device->StartRealPlay(media_id, play_info);
	else{
		device::info::InvalidOperation io;
		io.what = 0;
		io.why = "device_id not found";
		throw io;
	}
}

bool Server::StopRealPlay(const std::string& media_id, const std::string& device_id){
	auto device = FindDevice(device_id);
	if(device)
		return device->StopRealPlay(media_id);
	else{
		device::info::InvalidOperation io;
		io.what = 0;
		io.why = "device_id not found";
		throw io;
	}
}
