#include "dhnetsdk.h"
#include "../server.h"

using namespace device::netsdk;

bool Server::InitSDK() {
}

bool Server::CleanSDK() {
}

bool Server::Login(const LoginAccount& account) {
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
