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
  std::cout<<pchDVRIP<<":"<<nDVRPort<<" disconnect"<<std::endl;
  auto pthis = (Server*)dwUser;
  auto device = pthis->FindDevice((SESSION_ID)lLoginID);
  if(device){
    device->DisConnect();
  }
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
           <<"addr="<<_account.addr
           <<", port="<<_account.port
           <<", user="<<_account.user
           <<", password="<<_account.password<< std::endl;

  NET_DEVICEINFO info;
  int err_code = 0;
  _login_id = (SESSION_ID) CLIENT_Login((char*)_account.addr.c_str(), _account.port,
                                        (char*)_account.user.c_str(), (char*)_account.password.c_str(),
                                        &info, &err_code);
  if (_login_id == 0) {
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

void Device::Logout(){
  std::cout<<"logout: "<<std::endl;
  std::cout<<CLIENT_Logout((LLONG)_login_id)<<std::endl;
}

int r = 0;
void Media::StartRealPlay(){
  std::cout<<"media: start realplay: "<<_play_info.channel<<std::endl;
  auto data_callback = [] (LLONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, LONG param, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    device::info::MediaPackage media;
    media.type = device::types::MediaType::MediaData;
    media.reserver = r++;
    media.payload = std::string(pBuffer, pBuffer + dwBufSize);
    pthis->HandleDate(media);
  };

  auto disconnect = []( LLONG lOperateHandle, EM_REALPLAY_DISCONNECT_EVENT_TYPE dwEventType, void* param, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->MediaFinish();
  };

  _handle_id = (SESSION_ID) CLIENT_StartRealPlay((LLONG)_login_id, _play_info.channel, 0,
                                                 _play_info.stream_type == device::types::StreamType::Main ?
                                                 DH_RType_Realplay_0 : DH_RType_Realplay_1,
                                                 data_callback, disconnect, (LDWORD)this);
  if (_handle_id == 0) {
    std::cout<<"startplay error: "<<CLIENT_GetLastError()<<std::endl;
  }
}

void Media::StopRealPlay(){
  std::cout<<"media: stoprealplay "<<std::endl;
  CLIENT_StopRealPlay((long)_handle_id);
}


void Media::PlayBackByTime(){
  auto data_callback = [] (LLONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufferSize, LDWORD dwUser)->int{
    auto pthis = (Media*)dwUser;
    device::info::MediaPackage media;
    media.type = device::types::MediaType::MediaData;
    media.reserver = 0;
    media.payload = std::string(pBuffer, pBuffer + dwBufferSize);
    pthis->HandleDate(media);
    return 1;
  };

  auto disconnect = []( LLONG lOperateHandle, EM_REALPLAY_DISCONNECT_EVENT_TYPE dwEventType, void* param, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->MediaFinish();
  };

  NET_TIME start_time{(DWORD)_play_info.start_time.year, (DWORD)_play_info.start_time.month, (DWORD)_play_info.start_time.day,
      (DWORD)_play_info.start_time.hour, (DWORD)_play_info.start_time.minute, (DWORD)_play_info.start_time.second};

  NET_TIME end_time{(DWORD)_play_info.end_time.year, (DWORD)_play_info.end_time.month, (DWORD)_play_info.end_time.day,
      (DWORD)_play_info.end_time.hour, (DWORD)_play_info.end_time.minute, (DWORD)_play_info.end_time.second};

  _handle_id = (SESSION_ID)CLIENT_StartPlayBackByTime((LLONG)_login_id, _play_info.channel, &start_time, &end_time, 0,
                                                      nullptr,       0,//pos
                                                      data_callback, (LDWORD)this,
                                                      disconnect,    (LDWORD)this);

  if (_handle_id == 0) {
    std::cout<<"Fail to start playback: "<<CLIENT_GetLastError()<<std::endl;
  }
}

void Media::StopPlayBack(){
  CLIENT_StopPlayBack((long)_handle_id);
}
