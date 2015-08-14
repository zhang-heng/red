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
 private:
  Device* FindDevice(std::string id);
  Client *_client;
  static int const workerCount = 20;
  int _listen_port;
  int _client_port;
  std::map<std::string, Device*> _devices;
  int GetRandomPort(int from, int to);

 public:
  Server(int client_port);
  void ServerStarted();
  ~Server();

  void TestBytes(const std::string& buffer) {
    return;
  }

  bool InitSDK() override;

  bool CleanSDK() override;

  bool Login(const device::info::LoginAccount& account, const std::string& device_id) override;

  bool Logout(const std::string& device_id) override;

  void GetVersion(std::string& _return) override;

  bool xGetStatus() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return false;
  }

  bool xUpdata() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool xRestart() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool xSetTime() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool xResetPassword() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool xPTZControl() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool xSerialStart() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool xSerialStop() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  void xSerialSend() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
  }

  bool xDownloadRecordByFile() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool xDownloadRecordByTime() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool xStopDownload() override {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool StartRealPlay(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) override;

  bool StopRealPlay(const std::string& media_id, const std::string& device_id) override;

  bool StartVoiceTalk(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  bool StopVoiceTalk(const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
    return true;
  }

  void SendVoiceData(const std::string& device_id, const std::string& media_id, const std::string& buffer) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;
  }

  bool PlayBackByTime(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl; return true;
  }

  bool StopPlayBack(const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;return true;
  }

  bool PlayBackNormalSpeed(const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;return true;
  }

  bool PlayBackPause(const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;return true;
  }

  bool PlayBackFast(const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl; return true;
  }

  bool PlayBackSlow(const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl; return true;
  }

  bool PlayBackSeek(const std::string& media_id, const std::string& device_id) {
    // Your implementation goes here
    std::cout<<"b"<<std::endl;return true;
  }
};
