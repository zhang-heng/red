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
MediaPayloadType::type to_media_type(DWORD t) {//ok
  switch (t) {
  case 0:  return MediaPayloadType::MediaData;
  case 1:  return MediaPayloadType::VideoFrame;
  case 3:  return MediaPayloadType::AudioData;
  default: return MediaPayloadType::PrivatePack;
  }
}

NET_TIME to_dvr_time(TimeInfo &t) {//ok
  NET_TIME tt;
  tt.dwYear = t.year; tt.dwMonth  = t.month;  tt.dwDay    = t.day;
  tt.dwHour = t.hour; tt.dwMinute = t.minute; tt.dwSecond = t.second;
  return tt;
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


/****************server method fun****************/
void CALLBACK DisConnectFunc(LLONG lLoginID, char *pchDVRIP, LONG nDVRPort, LDWORD dwUser){//ok
  auto pthis = (Server*)dwUser;
  auto device = pthis->FindDevice((SESSION_ID)lLoginID);
  if(device){
    device->_Offline();
  }
}

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

/****************device method fun****************/
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
    _Log("fail to login, " + LoginErrorTostring(err_code));
    _Offline();
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
  _Online();
}

void Device::Logout(){//ok
  CLIENT_Logout((LLONG)_login_id);
}

/****************media method fun****************/
SESSION_ID Media::StartRealPlay(SESSION_ID login_id, long channel,
                                device::types::StreamType::type stream_type, device::types::ConnectType::type){
  cout<<"media: start realplay: "<<channel<<endl;
  auto data_callback = [] (LLONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, LONG param, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->_HandleDate((char*)pBuffer, (long)dwBufSize, to_media_type(dwDataType));
  };

  auto disconnect = []( LLONG lOperateHandle, EM_REALPLAY_DISCONNECT_EVENT_TYPE dwEventType, void* param, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->_MediaFinish();
  };

  auto handle_id = (SESSION_ID) CLIENT_StartRealPlay((LLONG)_login_id, channel, 0,
                                                 stream_type == device::types::StreamType::Main ?
                                                 DH_RType_Realplay_0 : DH_RType_Realplay_1,
                                                 data_callback, disconnect, (LDWORD)this);
  if (handle_id == 0) {
    _Log("startplay error: " + CLIENT_GetLastError());
    return 0;
  }
  return (SESSION_ID)handle_id;
}

void Media::StopRealPlay(SESSION_ID handle_id){
  cout<<"media: stoprealplay "<<endl;
  CLIENT_StopRealPlay((long)handle_id);
}


SESSION_ID Media::StartPlaybackByTime(SESSION_ID login_id, long channel,
                                      TimeInfo start_time, TimeInfo end_time,
                                      StreamType::type stream_type, ConnectType::type){
  auto st = to_dvr_time(start_time);
  auto et = to_dvr_time(end_time);

  auto pos_callback = []( LLONG lPlayHandle, DWORD dwTotalSize, DWORD dwDownLoadSize, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->_playback_total = dwTotalSize*1000;
    pthis->_playback_pos = dwDownLoadSize*1000;
  };

  auto data_callback = [] (LLONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufferSize, LDWORD dwUser)->int{
    auto pthis = (Media*)dwUser;
    pthis->_HandleDate((char*)pBuffer, (long)dwBufferSize, to_media_type(dwDataType),
                       pthis->_playback_pos, pthis->_playback_total, true);
    return 1;
  };

  auto disconnect = []( LLONG lOperateHandle, EM_REALPLAY_DISCONNECT_EVENT_TYPE dwEventType, void* param, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->_MediaFinish();
  };

  auto handle_id = (SESSION_ID)CLIENT_StartPlayBackByTime((LLONG)login_id, channel, &st, &et, 0,
                                                      pos_callback,  (LDWORD)this,
                                                      data_callback, (LDWORD)this,
                                                      disconnect,    (LDWORD)this);
  if (handle_id == 0) {
    cout<<"Fail to start playback: "<<CLIENT_GetLastError()<<endl;
    return 0;
  }
  return (SESSION_ID) handle_id;
}

void Media::StopPlaybackByTime(SESSION_ID handle_id){
  CLIENT_StopPlayBack((long)handle_id);
}

SESSION_ID Media::StartVoiceTalk(SESSION_ID login_id, long channel,
                                 StreamType::type stream_type, ConnectType::type){
  auto data_callback = [] (LLONG lTalkHandle, char *pDataBuf, DWORD dwBufSize, BYTE byAudioFlag, LDWORD dwUser){
    auto pthis = (Media*)dwUser;
    pthis->_HandleDate(pDataBuf, dwBufSize, MediaPayloadType::TalkData);
  };
  auto handle_id = (SESSION_ID)CLIENT_StartTalkEx((LLONG)_login_id, data_callback, (LDWORD)this);
  if (!handle_id) {
    cout<<"Fail to start voice talk, code:"<<CLIENT_GetLastError()<<endl;
    return 0;
  }
  return (SESSION_ID) handle_id;
}

void Media::StopVoiceTalk(SESSION_ID handle_id){
  CLIENT_StopTalkEx((long)handle_id);
}

void Media::SendVoiceData(SESSION_ID handle_id, const std::string& buffer){
  CLIENT_TalkSendData((long)handle_id, const_cast<char*>(buffer.c_str()), buffer.size());
}


void Media::PlayBackNormalSpeed(){
}
void Media::PlayBackPause(){
}
void Media::PlayBackFast(){
}
void Media::PlayBackSlow(){
}
void Media::PlayBackSeek(){
}
