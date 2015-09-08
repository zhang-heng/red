#ifndef __SERVER_DEVICE
#define __SERVER_DEVICE

#include "server_media.h"
#include "client.h"
#include <mutex>
#include <memory>

class Device{
 public:
  Device(std::string device_id, const device::info::LoginAccount& account, int client_port);
  void _Log(std::string msg);
  void _Online();
  void _Offline();
  SESSION_ID LoginID();
  std::shared_ptr<Media> FindMedia(std::string id);
  std::shared_ptr<Media> FindMedia(SESSION_ID id);

  void StartMedia(const device::info::PlayInfo& play_info, const std::string& media_id);
  void StopMedia(const std::string& media_id);

  void SendMediaData(const std::string& buffer, const std::string& media_id);

  void PlayBackNormalSpeed(const std::string& media_id);
  void PlayBackPause(const std::string& media_id);
  void PlayBackFast(const std::string& media_id);
  void PlayBackSlow(const std::string& media_id);
  void PlayBackSeek(const std::string& media_id);

  void Login(); //SDK
  void Logout();//SDK

  bool GetStatus(){};//SDK
  bool Updata(){};//SDK
  bool Restart(){};//SDK
  bool SetTime(){};//SDK
  bool ResetPassword(){};//SDK
  bool PTZControl(){};//SDK
  bool SerialStart(){};//SDK
  bool SerialStop(){};//SDK
  void SerialSend(){};//SDK

 private:
  SESSION_ID _login_id;
  std::string _device_id;
  device::info::LoginAccount _account;
  device::info::DeviceInfo _info;
  std::map<std::string, std::shared_ptr<Media>> _medias;
  std::mutex _medias_mtx;
  Client* _client;
  int _client_port;
};

#endif
