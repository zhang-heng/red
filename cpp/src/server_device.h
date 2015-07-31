#include "server_media.h"

class Device{
 public:
  Device(std::string device_id, const device::info::LoginAccount& account);
  bool Login();
  bool Login(const device::info::LoginAccount& account);
  bool Logout();
  bool StartRealPlay(const std::string& media_id, const device::info::PlayInfo& play_info);
  bool StopRealPlay(const std::string& media_id);
 private:
  SESSION_ID _login_id;
  std::string _device_id;
  device::info::LoginAccount _account;

  device::info::DeviceInfo _info;

  std::map<std::string, Media*> _medias;
  Media* FindMedia(std::string id);
};
