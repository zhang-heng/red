#include "Sdk.h"
#include "client.h"

#include <map>

class Server : virtual public device::netsdk::SdkIf {
 public:
  Server(){};
  Server(int client_port);
  void ServerStarted();
  ~Server();

  void Testing(const int32_t Bps);
  bool InitSDK();
  bool CleanSDK();
  bool Login(const device::netsdk::LoginAccount& account);
  bool Logout(const std::string& device_id);
  bool StartRealPlay(const std::string& device_id, const device::netsdk::PlayInfo& play_info);
  bool StopRealPlay(const std::string& device_id, const std::string& media_id);
  bool StartVoiceTalk(const std::string& device_id, const device::netsdk::PlayInfo& play_info);
  void SendVoiceData(const std::string& media_id, const std::string& buffer);
  bool StopVoiceTalk(const std::string& device_id, const std::string& media_id);
  bool PlayBackByTime(const std::string& device_id, const device::netsdk::PlayInfo& play_info);
  bool StopPlayBack(const std::string& device_id, const std::string& media_id);
  bool PlayBackNormalSpeed(const std::string& device_id, const std::string& media_id);
  bool PlayBackPause(const std::string& device_id, const std::string& media_id);
  bool PlayBackFast(const std::string& device_id, const std::string& media_id);
  bool PlayBackSlow(const std::string& device_id, const std::string& media_id);
  bool PlayBackSeek(const std::string& device_id, const std::string& media_id);

  void xGetVersion(std::string& _return){};
  bool xGetStatus(){};
  bool xUpdata(){};
  bool xRestart(){};
  bool xSetTime(){};
  bool xPTZControl(){};
  bool xSerialStart(){};
  bool xSerialSend(){};
  bool xSerialStop(){};
  bool xDownloadRecordByFile(){};
  bool xDownloadRecordByTime(){};
  bool xStopDownload(){};

 private:
  Client *client;
  int const workerCount = 20;
  int _listen_port;
  int _client_port;
  std::map<std::string, long> _devices;
  std::map<long, std::string> _sources;
  int GetRandomPort(int from, int to);
};
