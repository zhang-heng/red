#include "../server.h"

#if (defined(WIN32) || defined(WIN64))
#include <windows.h>
#endif

#include "HCNetSDK.h"

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"HCNetSDK.lib")
#pragma warning(disable:4800)
#endif

using namespace device;
using namespace device::netsdk;
using namespace device::types;

MediaType::type to_media_type(int t) {
  switch (t) {
  case NET_DVR_SYSHEAD:         return MediaType::FileHeader;
  case NET_DVR_STREAMDATA:      return MediaType::MediaData;
  case NET_DVR_AUDIOSTREAMDATA: return MediaType::AudioData;
  case NET_DVR_PRIVATE_DATA:    return MediaType::PrivatePack;
  default:                      return MediaType::PrivatePack;
  }
}

NET_DVR_TIME to_dvr_time(info::TimeInfo &t) {
  return NET_DVR_TIME {(DWORD)t.year, (DWORD)t.month, (DWORD)t.day,
      (DWORD)t.hour, (DWORD)t.minute, (DWORD)t.second};
}





void Server::InitSDK() {
  bool ret = NET_DVR_Init();
  if (!ret) {
    std::cout<<"Fail to init sdk"<<std::endl;
  }
}

void Server::CleanSDK() {
  NET_DVR_Cleanup();
}

void Server::GetVersion(std::string& _return){
  _return = "";
}

void Device::Login(){
  NET_DVR_DEVICEINFO_V30 info;
  _login_id = (SESSION_ID) NET_DVR_Login_V30((char*)_account.addr.c_str(),
                                             _account.port,
                                             (char*)_account.user.c_str(),
                                             (char*)_account.password.c_str(),
                                             &info);
  if (_login_id < 0) {}
}

void Device::Logout(){
  NET_DVR_Logout((long)_login_id);
}

void Media::StartRealPlay(){
  NET_DVR_PREVIEWINFO info;
  memset(&info, 0, sizeof(NET_DVR_PREVIEWINFO));
  info.lChannel     = _play_info.channel;
  info.dwStreamType = _play_info.stream_type == StreamType::Main ? 0 : 1;
  info.dwLinkMode   = _play_info.connect_type == ConnectType::Tcp ? 0 : 1;

  auto data_callback = [] (LONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, void *pUser){

  };
  auto handle_id = NET_DVR_RealPlay_V40((long)_login_id, &info, data_callback, 0);

  if (handle_id == -1) {
    LONG err = NET_DVR_GetLastError();
    std::cout<<NET_DVR_GetErrorMsg((LONG*)&err)<<std::endl;
  }
  _handle_id = (SESSION_ID)handle_id;
}

void Media::StopRealPlay(){
  NET_DVR_StopRealPlay((long)_handle_id);
}

void Media::PlayBackByTime(){
  NET_DVR_TIME st = to_dvr_time(_play_info.start_time);
  NET_DVR_TIME et  = to_dvr_time(_play_info.end_time);
  auto data_callback = [] (LONG lPlayHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, void *pUser){
  };

  auto session_id = NET_DVR_PlayBackByTime((long)_login_id, _play_info.channel, &st, &et, 0);
  if (session_id == -1){
  }

  auto ret_code = NET_DVR_PlayBackControl(session_id, NET_DVR_PLAYSTART, 0, 0);
  if (!ret_code){
    NET_DVR_StopPlayBack(session_id);
  };

  if (!NET_DVR_SetPlayDataCallBack_V40(session_id, data_callback, 0)){
    NET_DVR_StopPlayBack(session_id);
  }
}

void Media::StopPlayBack(){
  NET_DVR_StopPlayBack((long)_handle_id);
}
