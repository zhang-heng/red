#ifndef __SERVER_MEDIA
#define __SERVER_MEDIA

#include <iostream>
#include "Sdk.h"
#include "client.h"

#define SESSION_ID void*

class Media{
 public:
  Media(Client *client, std::string device_id, SESSION_ID login_id,
        std::string media_id,  const device::info::PlayInfo& play_info);
  void HandleDate();
  void StartRealPlay();  //SDK
  void StopRealPlay();   //SDK
  void StartVoiceTalk(){ }; //SDK
  void StopVoiceTalk(){ };  //SDK
  void SendVoiceData(const std::string& buffer){}; //SDK
  void PlayBackByTime(){ };      //SDK
  void StopPlayBack(){ };        //SDK
  void PlayBackNormalSpeed(){ }; //SDK
  void PlayBackPause(){ };       //SDK
  void PlayBackFast(){ };        //SDK
  void PlayBackSlow(){ };        //SDK
  void PlayBackSeek(){ };        //SDK
 private:
  Client* _client;
  std::string _device_id;
  SESSION_ID _login_id;

  std::string _media_id;
  SESSION_ID _handle_id;

  device::info::PlayInfo _play_info;
};

#endif
