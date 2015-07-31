#include "server_device.h"

Device::Device(std::string device_id, const device::info::LoginAccount& account){}

Media* Device::FindMedia(std::string id){
  auto it = _medias.find(id);
  if(it == _medias.end())
    return nullptr;
  return it->second;
}

bool Device::Login(const device::info::LoginAccount& account){
  Logout();
  _account = account;
  return Login();
}

bool Device::StartRealPlay(const std::string& media_id, const device::info::PlayInfo& play_info){
  auto media = FindMedia(media_id);
  if(media){
    device::info::InvalidOperation io;
    io.what = 0;
    io.why = "realplay has started";
    throw io;
  }
  else{
    media = new Media(_device_id, _login_id, media_id, play_info);
    _medias.insert(std::pair<std::string, Media*>(media_id, media));
    return media->StartRealPlay();
  }
}

bool Device::StopRealPlay(const std::string& media_id){
  auto media = FindMedia(media_id);
  if(media){
    _medias.erase(media_id);
    auto ret = media->StopRealPlay();
    delete media;
    return ret;
  }
  else{
    device::info::InvalidOperation io;
    io.what = 0;
    io.why = "realplay not found";
    throw io;
  }
}
