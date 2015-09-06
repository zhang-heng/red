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
MediaType::type to_media_type(DWORD t) {//ok
  switch (t) {
  case NET_DVR_SYSHEAD:         return MediaType::FileHeader;
  case NET_DVR_STREAMDATA:      return MediaType::MediaData;
  case NET_DVR_AUDIOSTREAMDATA: return MediaType::AudioData;
  case NET_DVR_PRIVATE_DATA:    return MediaType::PrivatePack;
  default:                      return MediaType::PrivatePack;
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
void Media::StartRealPlay(){//ok
  NET_DVR_PREVIEWINFO info;
  memset(&info, 0, sizeof(NET_DVR_PREVIEWINFO));
  info.lChannel     = _play_info.channel;
  info.dwStreamType = _play_info.stream_type == StreamType::Main ? 0 : 1;
  info.dwLinkMode   = _play_info.connect_type == ConnectType::Tcp ? 0 : 1;

  auto data_callback = [] (LONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, void *pUser){
    auto pthis = (Media*)pUser;
    device::info::MediaPackage media;
    media.type = to_media_type(dwDataType);
    media.payload = string(pBuffer, pBuffer + dwBufSize);
    pthis->_HandleDate(media);
  };
  auto handle_id = NET_DVR_RealPlay_V40((long)_login_id, &info, data_callback, this);

  if (handle_id == -1) {
    LONG err = NET_DVR_GetLastError();
    stringstream ss;
    ss<<"fail to realplay, "<<err<<"."<<NET_DVR_GetErrorMsg((LONG*)&err);
    _Log(ss.str());
    _MediaFinish();
    return;
  }
  _handle_id = (SESSION_ID)handle_id;
  _MediaStart();
}

void Media::StopRealPlay(){//ok
  NET_DVR_StopRealPlay((long)_handle_id);
}

void Media::PlayBackByTime(){//ok
  NET_DVR_TIME st = to_dvr_time(_play_info.start_time);
  NET_DVR_TIME et  = to_dvr_time(_play_info.end_time);
  auto data_callback = [] (LONG lPlayHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, void *pUser){
    auto pthis = (Media*)pUser;
    device::info::MediaPackage media;
    media.type = to_media_type(dwDataType);
    media.payload = string(pBuffer, pBuffer + dwBufSize);
    pthis->_HandleDate(media);
  };

  auto session_id = NET_DVR_PlayBackByTime((long)_login_id, _play_info.channel, &st, &et, 0);
  if (session_id == -1){
    _MediaFinish();
    return;
  }
  if (!NET_DVR_SetPlayDataCallBack_V40(session_id, data_callback, this)){
    NET_DVR_StopPlayBack(session_id);
    _MediaFinish();
    return;
  }
  if (!NET_DVR_PlayBackControl(session_id, NET_DVR_PLAYSTART, 0, 0)){
    NET_DVR_StopPlayBack(session_id);
    _MediaFinish();
    return;
  };
  _handle_id = (SESSION_ID) session_id;
  _MediaStart();
}

void Media::StopPlayBack(){//ok
  NET_DVR_StopPlayBack((long)_handle_id);
}

void Media::StartVoiceTalk(){
}

void Media::StopVoiceTalk(){
}

void Media::SendVoiceData(const std::string& buffer){
}
