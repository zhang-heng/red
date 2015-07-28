#include "Sdk.h"
#include "client.h"

class Server : virtual public device::netsdk::SdkIf {
 public:
  Server(){};
  Server(int client_port);
  void ServerStarted();
  ~Server();

  void Testing(const int32_t Bps);
  bool InitSDK();
  bool CleanSDK();
  bool Login(const device::netsdk::LoginAccount& info);
  bool Logout(const std::string& loginID);
  void xGetVersion(std::string& _return);
  bool xGetStatus();
  bool xUpdata();
  bool xRestart();
  bool xSetTime();
  bool xPTZControl();
  bool xSerialStart();
  bool xSerialSend();
  bool xSerialStop();
  bool xDownloadRecordByFile();
  bool xDownloadRecordByTime();
  bool xStopDownload();
  bool StartRealPlay(const std::string& device_id, const device::netsdk::PlayInfo& play_info);
  bool StopRealPlay(const std::string& device_id, const std::string& media_id);
  bool StartVoiceTalk(const std::string& device_id, const device::netsdk::PlayInfo& play_info);
  bool SendVoiceData(const std::string& media_id, const std::string& buffer);
  bool StopVoiceTalk(const std::string& device_id, const std::string& media_id);
  bool PlayBackByTime(const std::string& device_id, const device::netsdk::PlayInfo& play_info);
  bool StopPlayBack(const std::string& device_id, const std::string& media_id);
  bool PlayBackNormalSpeed(const std::string& device_id, const std::string& media_id);
  bool PlayBackPause(const std::string& device_id, const std::string& media_id);
  bool PlayBackFast(const std::string& device_id, const std::string& media_id);
  bool PlayBackSlow(const std::string& device_id, const std::string& media_id);
  bool PlayBackSeek(const std::string& device_id, const std::string& media_id);

 private:
  Client *client;
  int const workerCount = 20;
  int _listen_port;
  int _client_port;

  int GetRandomPort(int from, int to);
};
