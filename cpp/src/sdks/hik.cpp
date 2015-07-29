#include "HCNetSDK.h"
#include "../server.h"

using namespace device::netsdk;

bool Server::InitSDK() {
  auto ret = NET_DVR_Init();
  if (!ret) {
    InvalidOperation io;
    io.what = ret;
    io.why = "Fail to init sdk";
    throw io;
  }
}

bool Server::CleanSDK() {
  NET_DVR_Cleanup();
}

bool Server::Login(const LoginAccount& account) {
  NET_DVR_DEVICEINFO_V30 info;
  long login_id = NET_DVR_Login_V30((char*)account.addr.c_str(),
                                    account.port,
                                    (char*)account.user.c_str(),
                                    (char*)account.password.c_str(),
                                    &info);
  if(login_id < 0){
    InvalidOperation io;
    io.what = NET_DVR_GetLastError();
    io.why = "Fail to login device";
    throw io;
  }
  _devices.insert(std::pair<std::string, long>(account.device_id, login_id));
  return true;
}

bool Server::Logout(const std::string& device_id) {
  auto it = _devices.find(device_id);
  if(it != _devices.end()){
    NET_DVR_Logout(it->second);
    _devices.erase(it);
  }
}

bool Server::StartRealPlay(const std::string& device_id, const PlayInfo& play_info) {
  auto it = _devices.find(device_id);
  if(it != _devices.end()){
    auto login_id = it->second;
    NET_DVR_PREVIEWINFO info;
    memset(&info, 0, sizeof(NET_DVR_PREVIEWINFO));
    info.lChannel     = play_info.channel;
    info.dwStreamType = play_info.stream_type == StreamType::Main ? 0 : 1;
    info.dwLinkMode   = play_info.connect_type == ConnectType::Tcp ? 0 : 1;

    auto data_callback = [device_id, this] (LONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, void *pUser){
      auto st = this->_sources.find(lRealHandle);
      if(st != this->_sources.end()){
        MediaPackage mp;
        mp.payload = std::string(pBuffer, pBuffer + dwBufSize);
        switch (dwDataType) {
        case NET_DVR_SYSHEAD:         mp.type = MediaType::FileHeader;break;
        case NET_DVR_STREAMDATA:      mp.type = MediaType::MediaData;break;
        case NET_DVR_AUDIOSTREAMDATA: mp.type = MediaType::AudioData;break;
        case NET_DVR_PRIVATE_DATA:    mp.type = MediaType::PrivatePack;break;
        default:                      mp.type = MediaType::PrivatePack;break;
        }
        client->send_media_data(device_id, st->second, mp);
      }
    };

    // long session_id = NET_DVR_RealPlay_V40(login_id, &info, data_callback, 0);

    // if (session_id == -1) {
    //   LONG err = NET_DVR_GetLastError();
    //   throw domain_error(string("Fail to start realplay, msg:") + string(NET_DVR_GetErrorMsg((LONG*)&err)));
    // }
    // return session_id;
  }
}

bool Server::StopRealPlay(const std::string& device_id, const std::string& media_id) {
  // Your implementation goes here
  printf("StopRealPlay\n");
}

bool Server::StartVoiceTalk(const std::string& device_id, const PlayInfo& play_info) {
  // Your implementation goes here
  printf("StartVoiceTalk\n");
}

void Server::SendVoiceData(const std::string& media_id, const std::string& buffer) {
  // Your implementation goes here
  printf("SendVoiceData\n");
}

bool Server::StopVoiceTalk(const std::string& device_id, const std::string& media_id) {
  // Your implementation goes here
  printf("StopVoiceTalk\n");
}

bool Server::PlayBackByTime(const std::string& device_id, const PlayInfo& play_info) {
  // Your implementation goes here
  printf("PlayBackByTime\n");
}

bool Server::StopPlayBack(const std::string& device_id, const std::string& media_id) {
  // Your implementation goes here
  printf("StopPlayBack\n");
}

bool Server::PlayBackNormalSpeed(const std::string& device_id, const std::string& media_id) {
  // Your implementation goes here
  printf("PlayBackNormalSpeed\n");
}

bool Server::PlayBackPause(const std::string& device_id, const std::string& media_id) {
  // Your implementation goes here
  printf("PlayBackPause\n");
}

bool Server::PlayBackFast(const std::string& device_id, const std::string& media_id) {
  // Your implementation goes here
  printf("PlayBackFast\n");
}

bool Server::PlayBackSlow(const std::string& device_id, const std::string& media_id) {
  // Your implementation goes here
  printf("PlayBackSlow\n");
}

bool Server::PlayBackSeek(const std::string& device_id, const std::string& media_id) {
  // Your implementation goes here
  printf("PlayBackSeek\n");
}
