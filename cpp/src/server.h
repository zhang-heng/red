#include "Sdk.h"

#include "client.h"

#include <vector>
#include <map>


#define SESSION_ID void*

class DeviceInfo{
 public:
  std::string _device_id;
  SESSION_ID _login_id;
  device::info::DeviceInfo _info;
};

class MediaInfo{
 public:
  std::string _device_id;
  std::string _media_id;
  SESSION_ID _handle_id;
};

class Server : virtual public device::netsdk::SdkIf {
 public:
  Server(){};
  Server(int client_port);
  void ServerStarted();
  ~Server();

  bool InitSDK() override;
  bool CleanSDK() override;
  bool Login(const  ::device::info::LoginAccount& account) override;
  bool Logout(const std::string& deviceID) override;

  bool StartRealPlay(const std::string& device_id, const std::string& media_id, const  ::device::info::PlayInfo& play_info) override;
  bool StopRealPlay(const std::string& device_id, const std::string& media_id) override {

  };

  bool StartVoiceTalk(const std::string& device_id, const std::string& media_id, const  ::device::info::PlayInfo& play_info) override {
    // Your implementation goes here
    printf("StartVoiceTalk\n");
  }

  bool StopVoiceTalk(const std::string& device_id, const std::string& media_id) override {
    // Your implementation goes here
    printf("StopVoiceTalk\n");
  }

  void SendVoiceData(const std::string& media_id, const std::string& buffer) override {
    // Your implementation goes here
    printf("SendVoiceData\n");
  }

  bool PlayBackByTime(const std::string& device_id, const std::string& media_id, const  ::device::info::PlayInfo& play_info) override {
    // Your implementation goes here
    printf("PlayBackByTime\n");
  }

  bool StopPlayBack(const std::string& device_id, const std::string& media_id) override {
    // Your implementation goes here
    printf("StopPlayBack\n");
  }

  bool PlayBackNormalSpeed(const std::string& device_id, const std::string& media_id) override {
    // Your implementation goes here
    printf("PlayBackNormalSpeed\n");
  }

  bool PlayBackPause(const std::string& device_id, const std::string& media_id) override {
    // Your implementation goes here
    printf("PlayBackPause\n");
  }

  bool PlayBackFast(const std::string& device_id, const std::string& media_id) override {
    // Your implementation goes here
    printf("PlayBackFast\n");
  }

  bool PlayBackSlow(const std::string& device_id, const std::string& media_id) override {
    // Your implementation goes here
    printf("PlayBackSlow\n");
  }

  bool PlayBackSeek(const std::string& device_id, const std::string& media_id) override {
    // Your implementation goes here
    printf("PlayBackSeek\n");
  }

  bool xGetVersion() override {
    // Your implementation goes here
    printf("xGetVersion\n");
  }

  bool xGetStatus() override {
    // Your implementation goes here
    printf("xGetStatus\n");
  }

  bool xUpdata() override {
    // Your implementation goes here
    printf("xUpdata\n");
  }

  bool xRestart() override {
    // Your implementation goes here
    printf("xRestart\n");
  }

  bool xSetTime() override {
    // Your implementation goes here
    printf("xSetTime\n");
  }

  bool xResetPassword() override {
    // Your implementation goes here
    printf("xResetPassword\n");
  }

  bool xPTZControl() override {
    // Your implementation goes here
    printf("xPTZControl\n");
  }

  bool xSerialStart() override {
    // Your implementation goes here
    printf("xSerialStart\n");
  }

  bool xSerialStop() override {
    // Your implementation goes here
    printf("xSerialStop\n");
  }

  void xSerialSend() override {
    // Your implementation goes here
    printf("xSerialSend\n");
  }

  bool xDownloadRecordByFile() override {
    // Your implementation goes here
    printf("xDownloadRecordByFile\n");
  }

  bool xDownloadRecordByTime() override {
    // Your implementation goes here
    printf("xDownloadRecordByTime\n");
  }

  bool xStopDownload() override {
    // Your implementation goes here
    printf("xStopDownload\n");
  }


 private:
  Client *client;
  int const workerCount = 20;
  int _listen_port;
  int _client_port;
  std::map<SESSION_ID, DeviceInfo*> _devices;
  std::map<SESSION_ID, MediaInfo*> _medias;
  int GetRandomPort(int from, int to);
  DeviceInfo* FindDeviceInfo(std::string id);
  MediaInfo* FindMediaInfo(std::string id);
};
