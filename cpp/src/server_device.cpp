#include "server_device.h"
#include <sstream>

Device::Device(std::string device_id, const device::info::LoginAccount& account, int client_port)
  :_device_id(device_id),_account(account), _client_port(client_port){
  _client = new Client(_client_port);
}

SESSION_ID Device::LoginID(){
  return _login_id;
}

void Device::_Log(std::string msg){
  std::stringstream ss;
  ss <<_account.addr<<":"<<_account.port<<". "<<msg;
  _client->send_log(ss.str());
}

void Device::_Online(){
  _client->send_connected(_device_id);
}

void Device::_Offline(){
  std::stringstream ss;
  ss<<_device_id<<" "<<_account.addr<<":"<<_account.port<<" "<<"disconnect";
  _client->send_log(ss.str());
  _client->send_offline(_device_id);
}

std::shared_ptr<Media> Device::FindMedia(std::string id){
  std::shared_ptr<int> p2 (nullptr);
  auto it = _medias.find(id);
  if(it == _medias.end())
    return nullptr;
  return it->second;
}

std::shared_ptr<Media> Device::FindMedia(SESSION_ID id){
  for(auto it=_medias.begin(); it!=_medias.end(); it++){
    if(it->second->HandleID() == id)
      return it->second;
  }
  return nullptr;
}

void Device::StartMedia(const device::info::PlayInfo& play_info, const std::string& media_id){
  if(!FindMedia(media_id)){
    auto media = (new Media(_client_port, _device_id, _login_id, this, media_id, play_info))->Pointer;
    _medias_mtx.lock();
    _medias.insert(std::pair<std::string, std::shared_ptr<Media> >(media_id, media));
    _medias_mtx.unlock();
    media->StartMedia();
  }
}

void Device::StopMedia(const std::string& media_id){
  auto media = FindMedia(media_id);
  if(media){
    _medias_mtx.lock();
    _medias.erase(media_id);
    _medias_mtx.unlock();
    media->StopMedia();
  }
}

void Device::SendMediaData(const std::string& buffer, const std::string& media_id){
  auto media = FindMedia(media_id);
  if(media){media->SendMediaData(buffer);}
}

void Device::PlayBackNormalSpeed(const std::string& media_id) {
  auto media = FindMedia(media_id);
  if(media){media->PlayBackNormalSpeed();}
}

void Device::PlayBackPause(const std::string& media_id) {
  auto media = FindMedia(media_id);
  if(media){media->PlayBackPause();}
}

void Device::PlayBackFast(const std::string& media_id) {
  auto media = FindMedia(media_id);
  if(media){media->PlayBackFast();}
}

void Device::PlayBackSlow(const std::string& media_id) {
  auto media = FindMedia(media_id);
  if(media){media->PlayBackSlow();}
}

void Device::PlayBackSeek(const std::string& media_id) {
  auto media = FindMedia(media_id);
  if(media){media->PlayBackSeek();}
}
