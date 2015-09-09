#include "../server.h"

#if (defined(WIN32) || defined(WIN64))
#include <windows.h>
#endif

#include "HCNetSDK.h"

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"HCNetSDK.lib")
#pragma warning(disable:4800)
#endif

using namespace device::netsdk;
using namespace device::info;
using namespace device::types;
using namespace std;

#include <sstream>

/****************help fun****************/
MediaPayloadType::type to_media_type(DWORD t) {//ok
  switch (t) {
  case NET_DVR_SYSHEAD:         return MediaPayloadType::FileHeader;
  case NET_DVR_STREAMDATA:      return MediaPayloadType::MediaData;
  case NET_DVR_AUDIOSTREAMDATA: return MediaPayloadType::AudioData;
  case NET_DVR_PRIVATE_DATA:    return MediaPayloadType::PrivatePack;
  default:                      return MediaPayloadType::PrivatePack;
  }
}

NET_DVR_TIME to_dvr_time(TimeInfo &t) {//ok
  NET_DVR_TIME tt;
  tt.dwYear = t.year; tt.dwMonth  = t.month;  tt.dwDay    = t.day;
  tt.dwHour = t.hour; tt.dwMinute = t.minute; tt.dwSecond = t.second;
  return tt;
}

/****************server method fun****************/
void Server::InitSDK() {//ok
  if (!NET_DVR_Init()) {
    auto ie = NET_DVR_GetLastError();
    InvalidOperation e;
    e.what = ie;
    e.why  = NET_DVR_GetErrorMsg((LONG*)&ie);
    throw e;
  }
}

void Server::CleanSDK() {//ok
  NET_DVR_Cleanup();
}

void Server::GetVersion(std::string& _return){//ok
  stringstream ss;
  auto bv = NET_DVR_GetSDKBuildVersion();
  auto v1 = bv>>24&0xff;
  auto v2 = bv>>16&0xff;
  auto v3 = bv>>8&0xff;
  auto v4 = bv>>0&0xff;
  ss<<v1<<"."<<v2<<"."<<v3<<"."<<v4;
  _return = ss.str();
}

/****************device method fun****************/
void Device::Login(){//ok
  NET_DVR_DEVICEINFO_V30 info;
  auto login_id = NET_DVR_Login_V30((char*)_account.addr.c_str(), _account.port,
                                    (char*)_account.user.c_str(), (char*)_account.password.c_str(),
                                    &info);
  if (login_id < 0) {
    auto err = NET_DVR_GetLastError();
    stringstream ss;
    ss<<"fail to login, "<<err<<"."<<NET_DVR_GetErrorMsg((LONG*)&err);
    _Log (ss.str());
    _Offline();
    return;
  }
  _login_id = (SESSION_ID)login_id;
  _Online();
}

void Device::Logout(){//ok
  NET_DVR_Logout((long)_login_id);
}

/****************media method fun****************/
SESSION_ID Media::StartRealPlay(SESSION_ID login_id, long channel,
                                device::types::StreamType::type stream_type, device::types::ConnectType::type connect_type){
  NET_DVR_PREVIEWINFO info;
  memset(&info, 0, sizeof(NET_DVR_PREVIEWINFO));
  info.lChannel     = channel;
  info.dwStreamType = stream_type == StreamType::Main ? 0 : 1;
  info.dwLinkMode   = connect_type == ConnectType::Tcp ? 0 : 1;

  auto data_callback = [] (LONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, void *pUser){
    auto pthis = (Media*)pUser;
    pthis->_HandleDate((char*)pBuffer, dwBufSize, to_media_type(dwDataType));
  };
  auto handle_id = NET_DVR_RealPlay_V40((long)login_id, &info, data_callback, this);

  if (handle_id == -1) {
    LONG err = NET_DVR_GetLastError();
    stringstream ss;
    ss<<"fail to realplay, "<<err<<"."<<NET_DVR_GetErrorMsg((LONG*)&err);
    _Log(ss.str());
    return 0;
  }
  return (SESSION_ID)handle_id;
}

void Media::StopRealPlay(SESSION_ID handle_id){
  NET_DVR_StopRealPlay((long)handle_id);
}

SESSION_ID Media::StartPlaybackByTime(SESSION_ID login_id, long channel,
                                      TimeInfo start_time, TimeInfo end_time,
                                      StreamType::type stream_type, ConnectType::type){
  NET_DVR_TIME st = to_dvr_time(start_time);
  NET_DVR_TIME et  = to_dvr_time(end_time);

  auto data_callback = [] (LONG lPlayHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, void *pUser){
    auto pthis = (Media*)pUser;
    pthis->_HandleDate((char*)pBuffer, dwBufSize, to_media_type(dwDataType));
  };

  auto session_id = NET_DVR_PlayBackByTime((long)login_id, channel, &st, &et, 0);
  if (session_id == -1){
    return 0;
  }
  if (!NET_DVR_SetPlayDataCallBack_V40(session_id, data_callback, this)){
    NET_DVR_StopPlayBack(session_id);
    return 0;
  }
  if (!NET_DVR_PlayBackControl(session_id, NET_DVR_PLAYSTART, 0, 0)){
    NET_DVR_StopPlayBack(session_id);
    return 0;
  };
  return (SESSION_ID) session_id;
}

void Media::StopPlaybackByTime(SESSION_ID handle_id){
  NET_DVR_StopPlayBack((long)handle_id);
}

SESSION_ID Media::StartVoiceTalk(SESSION_ID login_id, long channel,
                                 StreamType::type stream_type, ConnectType::type){
  auto data_callback = [] (LONG lVoiceComHandle, char *pRecvDataBuffer, DWORD dwBufSize, BYTE byAudioFlag, void *pUser) {
    auto pthis = (Media*)pUser;
    pthis->_HandleDate(pRecvDataBuffer, dwBufSize, MediaPayloadType::TalkData);
  };
  long session_id = NET_DVR_StartVoiceCom_MR_V30((LONG)login_id, channel, data_callback, this);
  if (session_id == -1){
    auto err = NET_DVR_GetLastError();
    NET_DVR_GetErrorMsg((LONG*)&err);
    return 0;
  }
  return (SESSION_ID)session_id;
}

void Media::StopVoiceTalk(SESSION_ID handle_id){
  NET_DVR_StopVoiceCom((LONG)handle_id);
}

void Media::SendVoiceData(SESSION_ID handle_id, const std::string& buffer){
  NET_DVR_VoiceComSendData((LONG)handle_id, const_cast<char*>(buffer.c_str()), buffer.size());
}
