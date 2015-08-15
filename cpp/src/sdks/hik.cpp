#include "../server.h"

#if (defined(WIN32) || defined(WIN64))
#include <windows.h>
#endif

#include "HCNetSDK.h"

#if (defined(WIN32) || defined(WIN64))
#pragma comment(lib,"HCNetSDK.lib")
#pragma warning(disable:4800)
#endif

using namespace device;
using namespace device::netsdk;

void Server::InitSDK() {
	bool ret = NET_DVR_Init();
	if (!ret) {
      std::cout<<"Fail to init sdk"<<std::endl;
	}
}

void Server::CleanSDK() { return;}

void Server::GetVersion(std::string& _return){
	_return = "";
}

void Device::Login(){return;}
void Device::Logout(){return;}

void Media::StartRealPlay(){}
void Media::StopRealPlay(){}; 
// bool Server::Login(const std::string& deviceID, const device::info::LoginAccount& account){
//   NET_DVR_DEVICEINFO_V30 info;
//   long login_id = NET_DVR_Login_V30((char*)account.addr.c_str(),
//                                     account.port,
//                                     (char*)account.user.c_str(),
//                                     (char*)account.password.c_str(),
//                                     &info);
//   if(login_id < 0){
//     info::InvalidOperation io;
//     io.what = NET_DVR_GetLastError();
//     io.why = "Fail to login device";
//     throw io;
//   }

//   // auto device = new DeviceInfo();
//   // device->_device_id = account.device_id;
//   // device->_login_id = (SESSION_ID)login_id;
//   // device->_info.serial_number = std::string(info.sSerialNumber, info.sSerialNumber + SERIALNO_LEN);
//   // device->_info.n_an_video_channels = info.byChanNum;
//   // device->_info.start_an_video_channel = info.byStartDChan;
//   // device->_info.n_alarm_in = info.byAlarmInPortNum;
//   // device->_info.n_alarm_out = info.byAlarmOutPortNum;
//   // device->_info.device_model = "hik";
//   // device->_info.n_an_video_channels = info.byChanNum;
//   // device->_info.n_audio_channels = info.byAudioChanNum;
//   // device->_info.start_an_video_channel = 1;
//   // device->_info.n_dig_video_channels = info.byHighDChanNum;
//   // device->_info.start_dig_video_channels = info.byStartDChan;
//   // device->_info.n_talk_channels = info.byAudioChanNum;
//   // device->_info.start_talk_channels = info.byStartDTalkChan;
//   // device->_info.n_com = 0;

//   //  _devices.insert(std::pair<SESSION_ID, DeviceInfo*>((SESSION_ID)login_id, device));
//   return true;
// }

// bool Server::Logout(const std::string& device_id) {
//   //  auto device = FindDeviceInfo(device_id);
//   //   if(device){
//   //     NET_DVR_Logout((long)device->_login_id);
//   //   }else{
//   //     info::InvalidOperation io;
//   //     io.what = 0;
//   //     io.why = "device not found";
//   //   }
// }

// bool Server::StartRealPlay(const std::string& device_id,
//                            const std::string& media_id,
//                            const ::device::info::PlayInfo& play_info){
//   // auto it = _devices.find(device_id);
//   // if(it != _devices.end()){
//   //   auto login_id = (long)it->second->_login_id;
//   //   NET_DVR_PREVIEWINFO info;
//   //   memset(&info, 0, sizeof(NET_DVR_PREVIEWINFO));
//   //   info.lChannel     = play_info.channel;
//   //   info.dwStreamType = play_info.stream_type == types::StreamType::Main ? 0 : 1;
//   //   info.dwLinkMode   = play_info.connect_type == types::ConnectType::Tcp ? 0 : 1;

//   //   auto data_callback = [](LONG lRealHandle, DWORD dwDataType, BYTE *pBuffer, DWORD dwBufSize, void *pUser){
//   //     auto info = (MediaInfo*)pUser;
//   //     info::MediaPackage media;
//   //     switch (dwDataType) {
//   //     case NET_DVR_SYSHEAD:         media.type = types::MediaType::FileHeader;break;
//   //     case NET_DVR_STREAMDATA:      media.type = types::MediaType::MediaData;break;
//   //     case NET_DVR_AUDIOSTREAMDATA: media.type = types::MediaType::AudioData;break;
//   //     case NET_DVR_PRIVATE_DATA:    media.type = types::MediaType::PrivatePack;break;
//   //     default:                      media.type = types::MediaType::PrivatePack;break;
//   //     }
//   //     media.payload = std::string(pBuffer, pBuffer + dwBufSize);
//   //     client->send_media_data(info->_device_id, info->_media_id, media);
//   //   };

//   //   auto media_info = new MediaInfo();
//   //   long session_id = NET_DVR_RealPlay_V40(login_id, &info, data_callback, media_info);
//   //   if (session_id == -1) {
//   //     delete media_info;
//   //     LONG err = NET_DVR_GetLastError();
//   //     throw domain_error(string("Fail to start realplay, msg:") + string(NET_DVR_GetErrorMsg((LONG*)&err)));
//   //   }

//   //   return session_id;
//   // }
// }
