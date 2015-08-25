#include "Sdk.h"

#include "server_device.h"
#include "server_media.h"
#include "client.h"

#include <vector>
#include <map>
#include <mutex>

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
  std::mutex _devices_mtx;
  int GetRandomPort(int from, int to);

 public:
  Server(int client_port);
  void ServerStarted();
  ~Server();

  Device* FindDevice(SESSION_ID id);
  Media* FindMedia(SESSION_ID id);

  void Test1(const ::device::info::MediaPackage& mp) {
	  std::cout<<"->sdkTest1"<<std::endl;
  }

  void Test2(const std::string& bytes) {
	  std::cout<<"->sdkTest2"<<std::endl;
  }

  void Test3( ::device::info::MediaPackage& _return) {
	  std::cout<<"->sdkTest3"<<std::endl;
	  _return.type = device::types::MediaType::FileHeader;
	  _return.payload = "return by sdk";
  }

  void Test4(std::string& _return) {
	  std::cout<<"->sdkTest4"<<std::endl;
	  _return = "return by sdk";
  }

  void InitSDK() override;

  void CleanSDK() override;

  void Login(const device::info::LoginAccount& account, const std::string& device_id) override;

  void Logout(const std::string& device_id) override;

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

  void StartRealPlay(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) override;
  void StopRealPlay(const std::string& media_id, const std::string& device_id) override;

  void StartVoiceTalk(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) {  }
  void StopVoiceTalk(const std::string& media_id, const std::string& device_id) { }
  void SendVoiceData(const std::string& device_id, const std::string& media_id, const std::string& buffer) {  }

  void PlayBackByTime(const  ::device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) override;
  void StopPlayBack(const std::string& media_id, const std::string& device_id) override;

  void PlayBackNormalSpeed(const std::string& media_id, const std::string& device_id) {  }
  void PlayBackPause(const std::string& media_id, const std::string& device_id) {  }
  void PlayBackFast(const std::string& media_id, const std::string& device_id) {  }
  void PlayBackSlow(const std::string& media_id, const std::string& device_id) {  }
  void PlayBackSeek(const std::string& media_id, const std::string& device_id) {
  }
};
