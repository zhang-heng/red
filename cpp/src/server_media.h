#ifndef __SERVER_MEDIA
#define __SERVER_MEDIA

#include <iostream>
#include "Sdk.h"

#define SESSION_ID void*

class Media{
 public:
  Media(std::string device_id, SESSION_ID login_id,
        std::string media_id,  const device::info::PlayInfo& play_info);
  bool StartRealPlay(){return true;};  //SDK
  bool StopRealPlay(){return true;};   //SDK
  bool StartVoiceTalk(){return true;}; //SDK
  bool StopVoiceTalk(){return true;};  //SDK
  void SendVoiceData(const std::string& buffer){}; //SDK
  bool PlayBackByTime(){return true;};      //SDK
  bool StopPlayBack(){return true;};        //SDK
  bool PlayBackNormalSpeed(){return true;}; //SDK
  bool PlayBackPause(){return true;};       //SDK
  bool PlayBackFast(){return true;};        //SDK
  bool PlayBackSlow(){return true;};        //SDK
  bool PlayBackSeek(){return true;};        //SDK
 private:
  std::string _device_id;
  SESSION_ID _login_id;

  std::string _media_id;
  SESSION_ID _handle_id;

  device::info::PlayInfo _play_info;
};

#endif
