#include "Sdk.h"
#include "client.h"

#include <vector>
#include <map>


#define SESSION_ID void*

class MediaInfo{
 public:
  std::string _MediaID;
  SESSION_ID _HandleID;
};


class DeviceInfo{
 public:
  MediaInfo* GetMedia(std::string MediaID);
  device::netsdk::DeviceInfo _info;
  std::string _DeviceID;
  SESSION_ID _LoginID;

 private:
  std::vector<MediaInfo> _medias;
};


class Server : virtual public device::netsdk::SdkIf {
 public:
  Server(){};
  Server(int client_port);
  void ServerStarted();
  ~Server();

  void Testing(const int32_t Bps) override;
  bool InitSDK() override;
  bool CleanSDK() override;
  bool Login(const device::netsdk::LoginAccount& account) override;
  bool Logout(const std::string& device_id) override;

  bool StartRealPlay(const std::string& device_id, const std::string& media_id, const device::netsdk::PlayInfo& play_info) override;
  bool StopRealPlay(const std::string& device_id, const std::string& media_id) override;

  bool StartVoiceTalk(const std::string& device_id, const std::string& media_id, const device::netsdk::PlayInfo& play_info) override;
  void SendVoiceData(const std::string& media_id, const std::string& buffer) override;
  bool StopVoiceTalk(const std::string& device_id, const std::string& media_id) override;

  bool PlayBackByTime(const std::string& device_id, const std::string& media_id, const device::netsdk::PlayInfo& play_info) override;
  bool StopPlayBack       (const std::string& device_id, const std::string& media_id) override;
  bool PlayBackNormalSpeed(const std::string& device_id, const std::string& media_id) override;
  bool PlayBackPause      (const std::string& device_id, const std::string& media_id) override;
  bool PlayBackFast       (const std::string& device_id, const std::string& media_id) override;
  bool PlayBackSlow       (const std::string& device_id, const std::string& media_id) override;
  bool PlayBackSeek       (const std::string& device_id, const std::string& media_id) override;

  void xGetVersion(std::string& _return) override {} ;
  bool xGetStatus() override {};
  bool xUpdata() override {};
  bool xRestart() override {};
  bool xSetTime() override {};
  bool xPTZControl() override {};
  bool xSerialStart() override {};
  bool xSerialSend() override {};
  bool xSerialStop() override {};
  bool xDownloadRecordByFile() override {};
  bool xDownloadRecordByTime() override {};
  bool xStopDownload() override {};

 private:
  Client *client;
  int const workerCount = 20;
  int _listen_port;
  int _client_port;
  std::vector<DeviceInfo*> _devices;
  int GetRandomPort(int from, int to);
};
