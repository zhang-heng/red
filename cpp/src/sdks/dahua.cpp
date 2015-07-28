#include "dhnetsdk.h"
#include "../server.h"

using namespace device::netsdk;

void Server::Testing(const int32_t Bps) {
  // Your implementation goes here
  printf("Testing\n");
}

bool Server::InitSDK() {
  // Your implementation goes here
  printf("InitSDK\n");
}

bool Server::CleanSDK() {
  // Your implementation goes here
  printf("CleanSDK\n");
}

bool Server::Login(const LoginAccount& info) {
  // Your implementation goes here
  printf("Login\n");
}

bool Server::Logout(const std::string& loginID) {
  // Your implementation goes here
  printf("Logout\n");
}

void Server::xGetVersion(std::string& _return) {
  // Your implementation goes here
  printf("xGetVersion\n");
}

bool Server::xGetStatus() {
  // Your implementation goes here
  printf("xGetStatus\n");
}

bool Server::xUpdata() {
  // Your implementation goes here
  printf("xUpdata\n");
}

bool Server::xRestart() {
  // Your implementation goes here
  printf("xRestart\n");
}

bool Server::xSetTime() {
  // Your implementation goes here
  printf("xSetTime\n");
}

bool Server::xPTZControl() {
  // Your implementation goes here
  printf("xPTZControl\n");
}

bool Server::xSerialStart() {
  // Your implementation goes here
  printf("xSerialStart\n");
}

bool Server::xSerialSend() {
  // Your implementation goes here
  printf("xSerialSend\n");
}

bool Server::xSerialStop() {
  // Your implementation goes here
  printf("xSerialStop\n");
}

bool Server::xDownloadRecordByFile() {
  // Your implementation goes here
  printf("xDownloadRecordByFile\n");
}

bool Server::xDownloadRecordByTime() {
  // Your implementation goes here
  printf("xDownloadRecordByTime\n");
}

bool Server::xStopDownload() {
  // Your implementation goes here
  printf("xStopDownload\n");
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

bool Server::SendVoiceData(const std::string& media_id, const std::string& buffer) {
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
