#include "server_media.h"
#include "client.h"

class Device{
 public:
  Device(std::string device_id, const device::info::LoginAccount& account, Client* client);
  bool Login();
  bool Logout();
  bool StartRealPlay(const std::string& media_id, const device::info::PlayInfo& play_info);
  bool StopRealPlay(const std::string& media_id);
 private:
  SESSION_ID _login_id;
  std::string _device_id;
  device::info::LoginAccount _account;
  device::info::DeviceInfo _info;
  std::map<std::string, Media*> _medias;
  Client* _client;

  Media* FindMedia(std::string id);
};
