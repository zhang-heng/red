#ifndef __SERVER_MEDIA
#define __SERVER_MEDIA

#include <iostream>
#include "Sdk.h"

#define SESSION_ID void*

class Media{
 public:
  Media(std::string device_id, SESSION_ID login_id,
        std::string media_id,  const device::info::PlayInfo& play_info);
  bool StartRealPlay(){};  //SDK
  bool StopRealPlay(){};   //SDK
  bool StartVoiceTalk(){}; //SDK
  bool StopVoiceTalk(){};  //SDK
  void SendVoiceData(const std::string& buffer){}; //SDK
  bool PlayBackByTime(){};      //SDK
  bool StopPlayBack(){};        //SDK
  bool PlayBackNormalSpeed(){}; //SDK
  bool PlayBackPause(){};       //SDK
  bool PlayBackFast(){};        //SDK
  bool PlayBackSlow(){};        //SDK
  bool PlayBackSeek(){};        //SDK
 private:
  std::string _device_id;
  SESSION_ID _login_id;

  std::string _media_id;
  SESSION_ID _handle_id;

  device::info::PlayInfo _play_info;
};

#endif
