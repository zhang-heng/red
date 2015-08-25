#include "../server.h"

#include "dhnetsdk.h"

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"dhnetsdk.lib")
#pragma warning(disable:4800)
#endif

#include <sstream>

using namespace device::netsdk;
using namespace device::info;
using namespace device::types;
using namespace std;

/****************help fun****************/
MediaType::type to_media_type(DWORD t) {//ok
  switch (t) {
  case 0:  return MediaType::MediaData;
  case 1:  return MediaType::VideoFrame;
  case 3:  return MediaType::AudioData;
  default: return MediaType::PrivatePack;
  }
}

NET_TIME to_dvr_time(TimeInfo &t) {//ok
  return NET_TIME {(DWORD)t.year, (DWORD)t.month, (DWORD)t.day,
      (DWORD)t.hour, (DWORD)t.minute, (DWORD)t.second};
}

void CALLBACK DisConnectFunc(LLONG lLoginID, char *pchDVRIP, LONG nDVRPort, LDWORD dwUser){//ok
  auto pthis = (Server*)dwUser;
  auto device = pthis->FindDevice((SESSION_ID)lLoginID);
  if(device){
    device->DisConnect();
  }
}

string LoginErrorTostring(int code){//ok
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


/****************method fun****************/
void Server::InitSDK() {//ok
  if(!CLIENT_Init(DisConnectFunc, (LDWORD)this)){
    InvalidOperation e;
    e.what = 0;
    e.why  = "Fail to init dahua sdk";
    throw e;
  }
}

void Server::CleanSDK() {//ok
  CLIENT_Cleanup();
}

void Server::GetVersion(string& _return) {//ok
  stringstream ss;
  auto version = CLIENT_GetSDKVersion();
  ss<<version/10000000<<"."<<version/1000000%10<<"."<<version/100000%10<<"."<<version%10000;
  _return = ss.str();
}

void Device::Login() {//ok
  cout<<"login: "
      <<"addr="<<_account.addr
      <<", port="<<_account.port
      <<", user="<<_account.user
      <<", password="<<_account.password<< endl;

  NET_DEVICEINFO info;
  int err_code = 0;
  _login_id = (SESSION_ID) CLIENT_Login((char*)_account.addr.c_str(), _account.port,
                                        (char*)_account.user.c_str(), (char*)_account.password.c_str(),
                                        &info, &err_code);
  if (_login_id == 0) {
    Log("fail to login, " + LoginErrorTostring(err_code));
    _client->send_offline(_device_id);
    return;
  };
  //序列号
  _info.serial_number = string(info.sSerialNumber, info.sSerialNumber + DH_SERIALNO_LEN);
  //报警输入个数
  _info.n_alarm_in = info.byAlarmInPortNum;
  //报警输出个数
  _info.n_alarm_out = info.byAlarmOutPortNum;
  //硬盘个数
  _info.n_disk = info.byDiskNum;
  _client->send_connected(_device_id);
}

void Device::Logout(){//ok
  CLIENT_Logout((LLONG)_login_id);
}

void Media::StartRealPlay(){//ok
  cout<<"media: start realplay: "<<_play_info.channel<<endl;
  auto data_callback = [] (LLONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, LONG param, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    device::info::MediaPackage media;
    media.type = to_media_type(dwDataType);
    media.payload = string(pBuffer, pBuffer + dwBufSize);
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
    Log("startplay error: " + CLIENT_GetLastError());
    MediaFinish();
  }
}

void Media::StopRealPlay(){//ok
  cout<<"media: stoprealplay "<<endl;
  CLIENT_StopRealPlay((long)_handle_id);
}


void Media::PlayBackByTime(){//ok
  auto st = to_dvr_time(_play_info.start_time);
  auto et = to_dvr_time(_play_info.end_time);

  auto pos_callback = []( LLONG lPlayHandle, DWORD dwTotalSize, DWORD dwDownLoadSize, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->_playback_total = dwTotalSize*1000;
    pthis->_playback_pos = dwDownLoadSize*1000;
  };

  auto data_callback = [] (LLONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufferSize, LDWORD dwUser)->int{
    auto pthis = (Media*)dwUser;
    device::info::MediaPackage media;
    media.type = to_media_type(dwDataType);
    media.pos = pthis->_playback_pos;
    media.total = pthis->_playback_total;
    media.block = true;
    media.payload = string(pBuffer, pBuffer + dwBufferSize);
    pthis->HandleDate(media);
    return 1;
  };
  auto disconnect = []( LLONG lOperateHandle, EM_REALPLAY_DISCONNECT_EVENT_TYPE dwEventType, void* param, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->MediaFinish();
  };

  _handle_id = (SESSION_ID)CLIENT_StartPlayBackByTime((LLONG)_login_id, _play_info.channel, &st, &et, 0,
                                                      pos_callback,  (LDWORD)this,
                                                      data_callback, (LDWORD)this,
                                                      disconnect,    (LDWORD)this);
  if (_handle_id == 0) {
    cout<<"Fail to start playback: "<<CLIENT_GetLastError()<<endl;
    MediaFinish();
  }
}

void Media::StopPlayBack(){//ok
  CLIENT_StopPlayBack((long)_handle_id);
}
