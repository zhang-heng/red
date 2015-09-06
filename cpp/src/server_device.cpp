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

Media* Device::FindMedia(std::string id){
  auto it = _medias.find(id);
  if(it == _medias.end())
    return nullptr;
  return it->second;
}

Media* Device::FindMedia(SESSION_ID id){
  for(auto it=_medias.begin(); it!=_medias.end(); it++){
    if(it->second->HandleID() == id)
      return it->second;
  }
  return nullptr;
}

void Device::StartRealPlay(const std::string& media_id, const device::info::PlayInfo& play_info){
  auto media = FindMedia(media_id);
  if(!media){
    media = new Media(_client_port, _device_id, _login_id, this, media_id, play_info);
    _medias_mtx.lock();
    std::cout<<"device: startrealplay<<<"<<_device_id<<std::endl;
    _medias.insert(std::pair<std::string, Media*>(media_id, media));
    _medias_mtx.unlock();
    media->StartRealPlay();
    std::cout<<">>>device: startrealplay "<<_device_id<<std::endl;
  }
}

void Device::StopRealPlay(const std::string& media_id){
  std::cout<<"device: stoprealplay "<<_device_id<<std::endl;
  auto media = FindMedia(media_id);
  if(media){
    _medias_mtx.lock();
    _medias.erase(media_id);
    _medias_mtx.unlock();
    media->StopRealPlay();
    delete media;
  }
}

void Device::StartPlayBack(const std::string& media_id, const device::info::PlayInfo& play_info){
  auto media = FindMedia(media_id);
  if(!media){
    media = new Media(_client_port, _device_id, _login_id, this, media_id, play_info);
    _medias_mtx.lock();
    std::cout<<"device: StartPlayBack<<<"<<_device_id<<std::endl;
    _medias.insert(std::pair<std::string, Media*>(media_id, media));
    _medias_mtx.unlock();
    std::cout<<
      play_info.start_time.year  <<"/"<<
      play_info.start_time.month <<"/"<<
      play_info.start_time.day   <<" "<<
      play_info.start_time.hour  <<":"<<
      play_info.start_time.minute<<":"<<
      play_info.start_time.second<<" - ";
    std::cout<<
      play_info.end_time.year  <<"/"<<
      play_info.end_time.month <<"/"<<
      play_info.end_time.day   <<" "<<
      play_info.end_time.hour  <<":"<<
      play_info.end_time.minute<<":"<<
      play_info.end_time.second<<std::endl;

    media->PlayBackByTime();
    std::cout<<">>>device: StartPlayBack "<<_device_id<<std::endl;
  }
}

void Device::StopPlayBack(const std::string& media_id){
  std::cout<<"device: StopPlayBack "<<_device_id<<std::endl;
  auto media = FindMedia(media_id);
  if(media){
    _medias_mtx.lock();
    _medias.erase(media_id);
    _medias_mtx.unlock();
    media->StopPlayBack();
    delete media;
  }
}

void Device::StartVoiceTalk(const std::string& media_id, const device::info::PlayInfo& play_info){
  auto media = FindMedia(media_id);
  if(!media){
    media = new Media(_client_port, _device_id, _login_id, this, media_id, play_info);
    _medias_mtx.lock();
    std::cout<<"device: StartVoiceTalk<<<"<<_device_id<<std::endl;
    _medias.insert(std::pair<std::string, Media*>(media_id, media));
    _medias_mtx.unlock();
    media->StartVoiceTalk();
    std::cout<<">>>device: StartVoiceTalk "<<_device_id<<std::endl;
  }
}

void Device::StopVoiceTalk(const std::string& media_id){
  std::cout<<"device: StopVoiceTalk "<<_device_id<<std::endl;
  auto media = FindMedia(media_id);
  if(media){
    _medias_mtx.lock();
    _medias.erase(media_id);
    _medias_mtx.unlock();
    media->StopPlayBack();
    delete media;
  }
}

void Device::SendVoiceData(const std::string& media_id, const std::string& buffer){
  auto media = FindMedia(media_id);
  if(media){
    media->SendVoiceData(buffer);
  }
}
