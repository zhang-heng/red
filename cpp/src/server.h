#include "Sdk.h"

#include "server_device.h"
#include "server_media.h"
#include "client.h"

#include <vector>
#include <map>
#include <mutex>
#include <memory>

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
  std::shared_ptr<Media> FindMedia(SESSION_ID id);

  void InitSDK() override;
  void CleanSDK() override;

  void Login(const device::info::LoginAccount& account, const std::string& device_id) override;
  void Logout(const std::string& device_id) override;

  void StartMedia(const device::info::PlayInfo& play_info, const std::string& media_id, const std::string& device_id) override;
  void StopMedia(const std::string& media_id, const std::string& device_id) override;

  void SendMediaData(const std::string& buffer, const std::string& media_id, const std::string& device_id) override;

  void PlayBackNormalSpeed(const std::string& media_id, const std::string& device_id) override;
  void PlayBackPause(const std::string& media_id, const std::string& device_id) override;
  void PlayBackFast(const std::string& media_id, const std::string& device_id) override;
  void PlayBackSlow(const std::string& media_id, const std::string& device_id) override;
  void PlayBackSeek(const std::string& media_id, const std::string& device_id) override;

  void GetVersion(std::string& _return) override; //SDK

  bool GetStatus(const std::string& device_id) override;
  bool Updata(const std::string& device_id) override;
  bool Restart(const std::string& device_id) override;
  bool SetTime(const std::string& device_id) override;
  bool ResetPassword(const std::string& device_id) override;
  bool PTZControl(const std::string& device_id) override;
  bool SerialStart(const std::string& device_id) override;
  bool SerialStop(const std::string& device_id) override;
  void SerialSend(const std::string& device_id) override;
};
