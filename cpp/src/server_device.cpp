#include "server_device.h"

Device::Device(std::string device_id, const device::info::LoginAccount& account, int client_port)
  :_device_id(device_id),_account(account), _client_port(client_port){
  _client = new Client(_client_port);
}

SESSION_ID Device::LoginID(){
  return _login_id;
}

void Device::DisConnect(){
  _client->send_offline(_device_id);
}

Media* Device::FindMedia(std::string id){
  auto it = _medias.find(id);
  if(it == _medias.end())
    return nullptr;
  return it->second;
}

void Device::StartRealPlay(const std::string& media_id, const device::info::PlayInfo& play_info){
  auto media = FindMedia(media_id);
  if(!media){
    media = new Media(_client_port, _device_id, _login_id, media_id, play_info);
    _medias_mtx.lock();
    std::cout<<"device: startrealplay<<<"<<_device_id<<std::endl;
    _medias.insert(std::pair<std::string, Media*>(media_id, media));
    _medias_mtx.unlock();
    media->StartRealPlay();
    std::cout<<">>>device: startrealplay"<<_device_id<<std::endl;

  }
}

void Device::StopRealPlay(const std::string& media_id){
  std::cout<<"device: stoprealplay"<<_device_id<<std::endl;
  auto media = FindMedia(media_id);
  if(media){
    _medias_mtx.lock();
    _medias.erase(media_id);
    _medias_mtx.unlock();
    media->StopRealPlay();
    delete media;
  }
}
