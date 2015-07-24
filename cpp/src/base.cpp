#include<iostream>
#include "client.h"

namespace sdk{
  bool init ();
  bool fini();
  bool connect();
  bool disconnect();
  bool realplay();
  bool stop_realplay();
  bool voice_talk();
  bool stop_voice_talk();
  bool send_voice_talk_data();
  bool playback();
  bool stop_playback();
  bool playback_pause();
  bool playback_resume();
  bool playback_shift_speed();
  bool playback_seek();
}

int main(int argn, char** args)
{

  return 0;
}
