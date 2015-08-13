#include "../server.h"

#include "dhnetsdk.h"

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"dhnetsdk.lib")
#endif

using namespace device::netsdk;


void DisConnect (LLONG lLoginID, char *pchDVRIP, LONG nDVRPort, LDWORD dwUser){
}

bool Server::InitSDK() {
	bool ret = CLIENT_Init((fDisConnect)DisConnect, (LDWORD) this);
	std::cout<<"init dahua sdk"<<std::endl;
	return ret;
}

bool Server::CleanSDK() {return true;}

bool Device::Login(){return true;}

bool Device::Logout(){return true;}