#ifndef __SERVER_MEDIA
#define __SERVER_MEDIA

#include <iostream>
#include "Sdk.h"
#include "client.h"

#define SESSION_ID void*

class Device;

class Media{
 public:
  Media(int client_port, std::string device_id, SESSION_ID login_id, Device* device,
        std::string media_id,  const device::info::PlayInfo& play_info);
  SESSION_ID HandleID();
  void _Log(std::string msg);
  void _HandleDate(const device::info::MediaPackage & media);
  void _MediaStart();
  void _MediaFinish();

  void StartMedia();
  void StopMedia();
  void SendMediaData(const std::string& buffer);

  void StartRealPlay();  //SDK
  void StopRealPlay();   //SDK

  void StartVoiceTalk(); //SDK
  void StopVoiceTalk();  //SDK
  void SendVoiceData(const std::string& buffer); //SDK

  void PlayBackByTime();      //SDK
  void StopPlayBack();        //SDK
  void PlayBackNormalSpeed(){ }; //SDK
  void PlayBackPause(){ };       //SDK
  void PlayBackFast(){ };        //SDK
  void PlayBackSlow(){ };        //SDK
  void PlayBackSeek(){ };        //SDK

  long _playback_pos;
  long _playback_total;

 private:
  Client* _client;
  Device* _device;

  std::string _device_id;
  SESSION_ID _login_id;

  std::string _media_id;
  SESSION_ID _handle_id;

  device::info::PlayInfo _play_info;
};

#endif
