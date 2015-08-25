#include "server_media.h"
#include "server_device.h"
#include <sstream>

Media::Media(int client_port, std::string device_id, SESSION_ID login_id, Device* device,
             std::string media_id, const device::info::PlayInfo& play_info):
  _playback_pos(0), _playback_total(0), _device(device),
  _device_id(device_id), _login_id(login_id),
  _media_id(media_id), _play_info(play_info){
  _client = new Client(client_port);
}

void Media::Log(std::string msg){
  std::stringstream ss;
  ss<<_play_info.channel<<". "<<msg;
  _device->Log(ss.str());
}

void Media::HandleDate(const device::info::MediaPackage & media){
  _client->send_media_data(media, _media_id, _device_id);
}

void Media::MediaFinish(){
  _client->send_media_finish(_media_id, _device_id);
}
