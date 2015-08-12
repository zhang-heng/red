#include "../server.h"

#include "dhnetsdk.h"

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"dhnetsdk.lib")
#endif

using namespace device::netsdk;

bool Server::InitSDK() {return true;}
bool Server::CleanSDK() {return true;}

bool Device::Login(){return true;}
bool Device::Logout(){return true;}