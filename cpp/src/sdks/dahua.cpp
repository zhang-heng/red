#include "../server.h"

#include "dhnetsdk.h"

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"dhnetsdk.lib")
#pragma warning(disable:4800)
#endif

#include <sstream>

using namespace device::netsdk;

void CALLBACK DisConnectFunc(LONG lLoginID, char *pchDVRIP, LONG nDVRPort, DWORD dwUser)
{
	auto pthis = (Server*)dwUser;
}

bool Server::InitSDK() {
	bool ret = CLIENT_Init(DisConnectFunc, (LDWORD)this);
	std::cout<<"init dahua sdk "<< ret <<std::endl;
	return ret;
}

bool Server::CleanSDK() {return true;}

void Server::GetVersion(std::string& _return){
	std::stringstream stream;
	auto version = CLIENT_GetSDKVersion();
	stream<<version/10000000<<"."<<version/1000000%10<<"."<<version/100000%10<<"."<<version%10000;
	_return = stream.str();
}

bool Device::Login(){return true;}

bool Device::Logout(){return true;}