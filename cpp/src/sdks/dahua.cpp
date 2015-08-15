#include "../server.h"

#include "dhnetsdk.h"

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"dhnetsdk.lib")
#pragma warning(disable:4800)
#endif

#include <sstream>

using namespace device::netsdk;

void CALLBACK DisConnectFunc(LLONG lLoginID, char *pchDVRIP, LONG nDVRPort, LDWORD dwUser)
{
	auto pthis = (Server*)dwUser;
	//todo...
}

std::string LoginErrorTostring(int code){
	switch (code) {
	case 1: return "密码不正确";
	case 2: return "用户名不存在";
	case 3: return "登录超时";
	case 4: return "帐号已登录";
	case 5: return "帐号已被锁定";
	case 6: return "帐号被列为黑名单";
	case 7: return "资源不足，系统忙";
	case 8: return "子连接失败";
	case 9: return "主连接失败";
	case 10: return "超过最大用户连接数";
	default: return "不知道耶";
	};
}

void Server::InitSDK() {
	bool ret = CLIENT_Init(DisConnectFunc, (LDWORD)this);
	std::cout<<"init dahua sdk "<< ret <<std::endl;
	return;
}

void Server::CleanSDK() {
	//todo...
	return;}

void Server::GetVersion(std::string& _return) {
	std::stringstream stream;
	auto version = CLIENT_GetSDKVersion();
	stream<<version/10000000<<"."<<version/1000000%10<<"."<<version/100000%10<<"."<<version%10000;
	_return = stream.str();
}

void Device::Login() {
	std::cout<<"login: "
		<<"addr= "<<_account.addr
		<<"port= "<<_account.port
		<<"user= "<<_account.user
		<<"password= "<<_account.password<< std::endl;

	NET_DEVICEINFO info;
	int err_code = 0;
	auto login_id = CLIENT_Login((char*)_account.addr.c_str(), _account.port,
		(char*)_account.user.c_str(), (char*)_account.password.c_str(),
		&info, &err_code);
	if (login_id == 0) {
		std::cout<< "Fail to login, " + LoginErrorTostring(err_code)<< std::endl;
		_client->send_offline(_device_id);
	};
	//序列号
	_info.serial_number = std::string(info.sSerialNumber, info.sSerialNumber + DH_SERIALNO_LEN);
	//报警输入个数
	_info.n_alarm_in = info.byAlarmInPortNum;
	//报警输出个数
	_info.n_alarm_out = info.byAlarmOutPortNum;
	//硬盘个数
	_info.n_disk = info.byDiskNum;

	std::cout<<_info.serial_number<<std::endl;
	_client->send_connected(_device_id);
	return;
}

void Device::Logout(){return;}
