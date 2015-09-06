#ifndef __SERVER_DEVICE
#define __SERVER_DEVICE

#include "server_media.h"
#include "client.h"
#include <mutex>

class Device{
 public:
  Device(std::string device_id, const device::info::LoginAccount& account, int client_port);
  void _Log(std::string msg);
  void _Online();
  void _Offline();
  SESSION_ID LoginID();
  Media* FindMedia(std::string id);
  Media* FindMedia(SESSION_ID id);

  void Login();
  void Logout();
  void StartRealPlay(const std::string& media_id, const device::info::PlayInfo& play_info);
  void StopRealPlay(const std::string& media_id);

  void StartPlayBack(const std::string& media_id, const device::info::PlayInfo& play_info);
  void StopPlayBack(const std::string& media_id);

  void StartVoiceTalk(const std::string& media_id, const device::info::PlayInfo& play_info);
  void StopVoiceTalk(const std::string& media_id);
  void SendVoiceData(const std::string& media_id, const std::string& buffer);

 private:
  SESSION_ID _login_id;
  std::string _device_id;
  device::info::LoginAccount _account;
  device::info::DeviceInfo _info;
  std::map<std::string, Media*> _medias;
  std::mutex _medias_mtx;
  Client* _client;
  int _client_port;
};

#endif
