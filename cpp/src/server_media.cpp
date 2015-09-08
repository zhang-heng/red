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

SESSION_ID Media::HandleID(){
  return _handle_id;
}

void Media::_Log(std::string msg){
  std::stringstream ss;
  ss<<_play_info.channel<<". "<<msg;
  _device->_Log(ss.str());
}

void Media::_HandleDate(const device::info::MediaPackage & media){
  _client->send_media_data(media, _media_id, _device_id);
}

void Media::_MediaFinish(){//todo...
  _client->send_media_finish(_media_id, _device_id);
}

void Media::_MediaStart(){
  _client->send_media_started(_device_id, _media_id);
}

void Media::StartMedia(){
  SESSION_ID handle_id =0;

  switch(_play_info.type){
  case device::types::MediaType::RealPlay:
    // if(RealPlay(_login_id, &handle_id, _play_info)){

    // }
    break;
  case device::types::MediaType::VoiceTalk:
    // if(RealPlay(_login_id, &handle_id, _play_info)){
    // }
    break;
  case device::types::MediaType::PlaybackByTime:
    // if(RealPlay(_login_id, &handle_id, _play_info)){
    // }
    break;
  case device::types::MediaType::PlaybackByFile:
    // if(RealPlay(_login_id, &handle_id, _play_info)){
    // }
    break;
  }
}

void Media::StopMedia(){
}

void Media::SendMediaData(const std::string& buffer){
}

    // std::cout<<
    //   play_info.start_time.year  <<"/"<<
    //   play_info.start_time.month <<"/"<<
    //   play_info.start_time.day   <<" "<<
    //   play_info.start_time.hour  <<":"<<
    //   play_info.start_time.minute<<":"<<
    //   play_info.start_time.second<<" - ";
    // std::cout<<
    //   play_info.end_time.year  <<"/"<<
    //   play_info.end_time.month <<"/"<<
    //   play_info.end_time.day   <<" "<<
    //   play_info.end_time.hour  <<":"<<
    //   play_info.end_time.minute<<":"<<
    //   play_info.end_time.second<<std::endl;
