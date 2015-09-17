#ifndef __SERVER_MEDIA
#define __SERVER_MEDIA

#include <iostream>
#include "Sdk.h"
#include "client.h"
#include <memory>

#define SESSION_ID void*

class Device;

class Media{
 public:
  std::shared_ptr<Media> Pointer;

  Media(int client_port, std::string device_id, SESSION_ID login_id, Device* device,
        std::string media_id,  const device::info::PlayInfo& play_info);
  ~Media(){
    std::cout<<"media released.---> "<< _media_id <<std::endl;
  };
  SESSION_ID HandleID();
  void _Log(std::string msg);
  void _HandleDate(char* pbuffer, long size, device::types::MediaPayloadType::type type, bool block = false);
  void _HandleDate(char* pbuffer, long size, device::types::MediaPayloadType::type type, long pos, long total, bool block = false);
  void _MediaStart();
  void _MediaFinish();

  void StartMedia();
  void StopMedia();
  void SendMediaData(const std::string& buffer);

  void PlayBackNormalSpeed(); //SDK
  void PlayBackPause();       //SDK
  void PlayBackFast();        //SDK
  void PlayBackSlow();        //SDK
  void PlayBackSeek();        //SDK

 private:
  Client* _client;
  Device* _device;

  std::string _device_id;
  SESSION_ID _login_id;

  std::string _media_id;
  SESSION_ID _handle_id;

  device::info::PlayInfo _play_info;

  long _playback_pos;
  long _playback_total;
  bool _working;

  SESSION_ID StartRealPlay(SESSION_ID login_id, long channel,
                           device::types::StreamType::type stream_type, device::types::ConnectType::type);//SDK
  void StopRealPlay(SESSION_ID handle_id);//SDK

  SESSION_ID StartPlaybackByTime(SESSION_ID login_id, long channel,
                                 device::info::TimeInfo start_time, device::info::TimeInfo end_time,
                                 device::types::StreamType::type stream_type, device::types::ConnectType::type);//SDK
  void StopPlaybackByTime(SESSION_ID handle_id);//SDK

  SESSION_ID StartVoiceTalk(SESSION_ID login_id, long channel,
                            device::types::StreamType::type stream_type, device::types::ConnectType::type);//SDK
  void StopVoiceTalk(SESSION_ID handle_id);//SDK
  void SendVoiceData(SESSION_ID handle_id, const std::string& buffer);//SDK

  SESSION_ID StartPlaybackByFile(SESSION_ID login_id, std::string path,
                                 device::types::StreamType::type stream_type, device::types::ConnectType::type){};//SDK
  void StopPlaybackByFile(SESSION_ID handle_id){};//SDK
};

#endif
