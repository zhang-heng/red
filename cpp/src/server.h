#include "Sdk.h"

#include "server_device.h"
#include "server_media.h"
#include "client.h"

#include <vector>
#include <map>

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"libthrift.lib")
#pragma comment(lib,"libeay32.lib")
#pragma comment(lib,"ssleay32.lib")
#pragma comment(lib,"main.lib")
#endif

class Server : virtual public device::netsdk::SdkIf {
 public:
  Server(){};
  Server(int client_port);
  void ServerStarted();
  ~Server();

  bool InitSDK() override;

  bool CleanSDK() override;

  bool Login(const std::string& device_id, const  ::device::info::LoginAccount& account) override;

  bool Logout(const std::string& device_id) override;

  bool xGetVersion() override {
	  // Your implementation goes here
	  printf("xGetVersion\n");
	  return true;
  }

  bool xGetStatus() override {
	  // Your implementation goes here
	  printf("xGetStatus\n");
	  return true;
  }

  bool xUpdata() override {
	  // Your implementation goes here
	  printf("xUpdata\n");
	  return true;
  }

  bool xRestart() override {
	  // Your implementation goes here
	  printf("xRestart\n");
	  return true;
  }

  bool xSetTime() override {
	  // Your implementation goes here
	  printf("xSetTime\n");
	  return true;
  }

  bool xResetPassword() override {
	  // Your implementation goes here
	  printf("xResetPassword\n"); return true;
  }

  bool xPTZControl() override {
	  // Your implementation goes here
	  printf("xPTZControl\n"); return true;
  }

  bool xSerialStart() override {
	  // Your implementation goes here
	  printf("xSerialStart\n"); return true;
  }

  bool xSerialStop() override {
	  // Your implementation goes here
	  printf("xSerialStop\n"); return true;
  }

  void xSerialSend() override {
	  // Your implementation goes here
	  printf("xSerialSend\n");
  }

  bool xDownloadRecordByFile() override {
	  // Your implementation goes here
	  printf("xDownloadRecordByFile\n"); return true;
  }

  bool xDownloadRecordByTime() override {
	  // Your implementation goes here
	  printf("xDownloadRecordByTime\n"); return true;
  }

  bool xStopDownload() override {
	  // Your implementation goes here
	  printf("xStopDownload\n"); return true;
  }

  bool StartRealPlay(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) override;

  bool StopRealPlay(const std::string& media_id, const std::string& device_id) override;

  bool StartVoiceTalk(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("StartVoiceTalk\n"); return true;
  }

  bool StopVoiceTalk(const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("StopVoiceTalk\n"); return true;
  }

  void SendVoiceData(const std::string& device_id, const std::string& media_id, const std::string& buffer) {
	  // Your implementation goes here
	  printf("SendVoiceData\n");
  }

  bool PlayBackByTime(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("PlayBackByTime\n"); return true;
  }

  bool StopPlayBack(const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("StopPlayBack\n"); return true;
  }

  bool PlayBackNormalSpeed(const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("PlayBackNormalSpeed\n"); return true;
  }

  bool PlayBackPause(const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("PlayBackPause\n"); return true;
  }

  bool PlayBackFast(const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("PlayBackFast\n"); return true;
  }

  bool PlayBackSlow(const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("PlayBackSlow\n"); return true;
  }

  bool PlayBackSeek(const std::string& media_id, const std::string& device_id) {
	  // Your implementation goes here
	  printf("PlayBackSeek\n"); return true;
  }


 private:
  Device* FindDevice(std::string id);
  Client *client;
  static int const workerCount = 20;
  int _listen_port;
  int _client_port;
  std::map<std::string, Device*> _devices;
  int GetRandomPort(int from, int to);
};
