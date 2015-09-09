#include "../server.h"

#include "netsdk.h"

using namespace device::netsdk;
using namespace device::info;
using namespace device::types;
using namespace std;

#include <sstream>

/****************help fun****************/
MediaPayloadType::type to_media_type(DWORD t) {//ok
  switch (t) {
  case FILE_HEAD:     return MediaPayloadType::FileHeader;
  case VIDEO_I_FRAME: return MediaPayloadType::VideoFrame;
  case AUDIO_PACKET:  return MediaPayloadType::AudioData;
  default:            return MediaPayloadType::MediaData;
  }
}

H264_DVR_TIME to_dvr_time(TimeInfo &t) {//ok
  return H264_DVR_TIME {t.year, t.month, t.day,
      t.hour, t.minute, t.second};
}

/****************server method fun****************/
void CALLBACK DisConnectFunc(long lLoginID, char *pchDVRIP, long nDVRPort, unsigned long dwUser){
  auto pthis = (Server*)dwUser;
  auto device = pthis->FindDevice((SESSION_ID)lLoginID);
  if(device){
    device->_Offline();
  }
}


void Server::InitSDK() {//ok
  if (!H264_DVR_Init(DisConnectFunc,(unsigned long)this)) {
    InvalidOperation e;
    e.what = 0;
    e.why  = "fail to init xm sdk.";
    throw e;
  }
}

void Server::CleanSDK() {//ok
  H264_DVR_Cleanup();
}

void Server::GetVersion(std::string& _return){//todo
  // stringstream ss;
  // auto bv = NET_DVR_GetSDKBuildVersion();
  // auto v1 = bv>>24&0xff;
  // auto v2 = bv>>16&0xff;
  // auto v3 = bv>>8&0xff;
  // auto v4 = bv>>0&0xff;
  // ss<<v1<<"."<<v2<<"."<<v3<<"."<<v4;
  // _return = ss.str();
}

/****************device method fun****************/
void Device::Login(){//ok
  int error_code = 0;
  H264_DVR_DEVICEINFO info;
  auto login_id = H264_DVR_Login((char*)_account.addr.c_str(), _account.port,
                                 (char*)_account.user.c_str(), (char*)_account.password.c_str(),
                                 &info, &error_code, TCPSOCKET);
  if (login_id == 0) {
    _Log ("fail to login");
    _Offline();
    return;
  }
  _login_id = (SESSION_ID)login_id;
  _Online();
}

void Device::Logout(){//ok
  H264_DVR_Logout((long)_login_id);
}

/****************media method fun****************/
SESSION_ID Media::StartRealPlay(SESSION_ID login_id, long channel,
                                device::types::StreamType::type stream_type, device::types::ConnectType::type connect_type){
  H264_DVR_CLIENTINFO info;
  info.nChannel = channel;
  info.nStream  = stream_type == StreamType::Main ? 0 : 1;
  info.nMode    = connect_type == ConnectType::Tcp ? 0 : 1;
  info.nComType = 0;
  info.hWnd     = nullptr;

  auto session_id = H264_DVR_RealPlay((long)login_id, &info);
  if (!session_id) {
    LONG err = H264_DVR_GetLastError();
    stringstream ss;
    ss<<"fail to realplay, "<<err;
    _Log(ss.str());
    return 0;
  }

  auto data_callback = [](long lRealHandle, long dwDataType, unsigned char *pBuffer,long lbufsize,long dwUser){
    auto pthis = (Media*)dwUser;
    pthis->_HandleDate((char*)pBuffer, lbufsize, to_media_type(dwDataType));
    return TRUE;
  };

  if (not H264_DVR_SetRealDataCallBack(session_id, data_callback, (long)this)) {
    H264_DVR_StopRealPlay(session_id);
  }

  return (SESSION_ID)session_id;
}

void Media::StopRealPlay(SESSION_ID handle_id){
  H264_DVR_StopRealPlay((long)handle_id);
}

SESSION_ID Media::StartPlaybackByTime(SESSION_ID login_id, long channel,
                                      TimeInfo start_time, TimeInfo end_time,
                                      StreamType::type stream_type, ConnectType::type){
  H264_DVR_FINDINFO info;
  info.nChannelN0    = channel;
  info.nFileType     = SDK_RECORD_ALL;
  info.startTime     = to_dvr_time(start_time);
  info.endTime       = to_dvr_time(end_time);
  info.szFileName[0] = '\0';
  info.hWnd          = nullptr;

  auto pos_callback = [](long lPlayHandle, long lTotalSize, long lDownLoadSize, long dwUser){
    auto pthis = (Media*)dwUser;
    pthis->_playback_total = lTotalSize;
    pthis->_playback_pos = lDownLoadSize;
  };
  auto data_callback = [] (long lRealHandle, long dwDataType, unsigned char *pBuffer,long lbufsize, long dwUser) -> int {
    auto pthis = (Media*)dwUser;
    pthis->_HandleDate((char*)pBuffer, lbufsize, to_media_type(dwDataType),
                       pthis->_playback_pos, pthis->_playback_total, true);
    return 1;
  };

  auto session_id = H264_DVR_PlayBackByTime((long)_login_id, &info, pos_callback, data_callback, (long)this);
  if (!session_id) {
    _Log("Fail to start playback: " + to_string(H264_DVR_GetLastError()));
    return 0;
  }
  return (SESSION_ID) session_id;
}

void Media::StopPlaybackByTime(SESSION_ID handle_id){
  H264_DVR_StopPlayBack((long)handle_id);
}

SESSION_ID Media::StartVoiceTalk(SESSION_ID login_id, long channel,
                                 StreamType::type stream_type, ConnectType::type){}

void Media::StopVoiceTalk(SESSION_ID handle_id){}

void Media::SendVoiceData(SESSION_ID handle_id, const std::string& buffer){}
