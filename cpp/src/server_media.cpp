#include "server_media.h"
#include "server_device.h"
#include <sstream>

using namespace std;
using namespace device::types;

Media::Media(int client_port, std::string device_id, SESSION_ID login_id, Device* device,
             std::string media_id, const device::info::PlayInfo& play_info):
  _playback_pos(0), _playback_total(0), _device(device), _working(false),
  _device_id(device_id), _login_id(login_id),
  _media_id(media_id), _play_info(play_info){
  _client = new Client(client_port);
  Pointer = shared_ptr<Media>(this);
}

SESSION_ID Media::HandleID(){
  return _handle_id;
}

void Media::_MediaFinish(){
  _client->send_media_finish(_media_id, _device_id);
}

void Media::_HandleDate(char* pbuffer, long size, MediaPayloadType::type type, bool block){
  _HandleDate(pbuffer, size, type, 0, 0, block);
}

void Media::_HandleDate(char* pbuffer, long size, MediaPayloadType::type type,
                        long pos, long total, bool block){
  device::info::MediaPackage media;
  media.type = type;
  media.pos = pos;
  media.total = total;
  media.block = block;
  media.payload = string (pbuffer, pbuffer + size);

  _client->send_media_data(media, _media_id, _device_id);
}

void Media::StartMedia(){
  auto pthis = Pointer;
  _working = true;

  switch(_play_info.type){
  case device::types::MediaType::RealPlay:
    _handle_id = StartRealPlay(_login_id, _play_info.channel, _play_info.stream_type, _play_info.connect_type);
    break;
  case device::types::MediaType::VoiceTalk:
    _handle_id = StartVoiceTalk(_login_id, _play_info.channel, _play_info.stream_type, _play_info.connect_type);
    break;
  case device::types::MediaType::PlaybackByTime:
    _handle_id = StartPlaybackByTime(_login_id, _play_info.channel, _play_info.start_time, _play_info.end_time,
                                     _play_info.stream_type, _play_info.connect_type);
    break;
  case device::types::MediaType::PlaybackByFile:
    _handle_id = StartPlaybackByFile(_login_id, _play_info.file_path, _play_info.stream_type, _play_info.connect_type);
    break;
  }
  if(!_working) {
    StopMedia();//过程中已关闭
    return;
  }
  if(_handle_id == 0) {//通知失败
    _MediaFinish();
  }
}

void Media::StopMedia(){
  Pointer = nullptr;
  _working = false;
  if(_handle_id)
    switch(_play_info.type){
    case device::types::MediaType::RealPlay:
      StopRealPlay(_handle_id);
      break;
    case device::types::MediaType::VoiceTalk:
      StopVoiceTalk(_handle_id);
      break;
    case device::types::MediaType::PlaybackByTime:
      StopPlaybackByTime(_handle_id);
      break;
    case device::types::MediaType::PlaybackByFile:
      StopPlaybackByFile(_handle_id);
      break;
    }
  _handle_id = 0;
}

void Media::SendMediaData(const std::string& buffer){
  if(_play_info.type == device::types::MediaType::VoiceTalk)
    SendVoiceData(_handle_id, buffer);
}

void Media::_Log(std::string msg){
  std::stringstream ss;
  ss<<_play_info.channel<<". "<<msg;
  _device->_Log(ss.str());
}

void Media::_MediaStart(){
  _client->send_media_started(_device_id, _media_id);
}
