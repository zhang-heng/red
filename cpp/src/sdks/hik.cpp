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
}

bool Server::Logout(const std::string& device_id) {
  //NET_DVR_Logout(login_id);
}

bool Server::StartRealPlay(const std::string& device_id, const PlayInfo& play_info) {
  // Your implementation goes here
  printf("StartRealPlay\n");
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
