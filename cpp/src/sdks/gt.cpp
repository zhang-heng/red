#include "../server.h"

#if (defined(WIN32) || defined(WIN64))
#include <windows.h>
#endif

#include "ip_netsdk.h"

#if (defined(WIN32) || defined(WIN64))
#pragma warning(disable:4800)
#endif

using namespace device::netsdk;
using namespace device::info;
using namespace device::types;
using namespace std;

#include <sstream>

string get_error_description(int id) {//ok
  switch(id){
  case ERR_DVC_INTERNAL:        return "设备内部错";
  case ERR_DVC_INVALID_REQ:     return "客户请求数据格式错";
  case ERR_DVC_BUSY:            return "设备忙";
  case ERR_DVC_FAILURE:         return "设备故障";
  case ERR_EVC_CRC_ERR:         return "设备收到一个crc错误的数据包";
  case ERR_EVC_NOT_SUPPORT:     return "设备收到一个不支持的命令";
  case ERR_ENC_NOT_ALLOW:       return "设备收到一个不允许的命令";
  case ERR_DVC_NO_RECORD_INDEX: return "设备没有查询到索引";
  case ERR_SDK_ENETDOWN:        return "网络禁用";
  case ERR_SDK_ENETUNREACH:     return "网络不可达";
  case ERR_SDK_ENETRESET:       return "连接断开";
  case ERR_SDK_ECONNABORTED:    return "放弃连接";
  case ERR_SDK_ECONNRESET:      return "连接复位";
  case ERR_SDK_ENOBUFS:         return "缓冲区不足";
  case ERR_SDK_EISCONN:         return "连接已建立";
  case ERR_SDK_ENOTCONN:        return "连接未建立";
  case ERR_SDK_ESHUTDOWN:       return "对方连接断开,不能发送";
  case ERR_SDK_ETOOMANYREFS:    return "cannot splice";
  case ERR_SDK_ETIMEDOUT:       return "连接超时";
  case ERR_SDK_ECONNREFUSED:    return "连接被拒绝";
  case ERR_SDK_EPERM:           return "不允许的操作";
  case ERR_SDK_ENOENT:          return "没有指定的文件或目录";
  case ERR_SDK_EBUSY:           return "设备或资源忙";

  case ERR_SDK_EINVAL:          return "参数错误";
  case ERR_SDK_EMFILE:          return "打开的文件过多";
  case ERR_SDK_EAGAIN:          return "稍后重试";
  case ERR_SDK_ENOMEM:          return "内存不足";
  case ERR_SDK_EFBIG:           return "文件过大";
  case ERR_SDK_UNKNOW:          return "未知错误";
  case ERR_SDK_ECFILE:          return "创建文件失败";
  case ERR_SDK_UNKNOW_CERT:	return "不支持的证书格式";
  case ERR_SDK_OP_TIMEOUT:      return "操作超时";
  case ERR_SDK_NOT_SUPPORT:     return "SDK不支持的操作";
  }
  return "";
}

MediaType::type to_media_type(int t) {//ok
  switch (t) {
  case FRAMETYPE_H:   return MediaType::FileHeader;
  case FRAMETYPE_V:   return MediaType::VideoFrame;
  case FRAMETYPE_A:   return MediaType::AudioData;
  default:            return MediaType::MediaData;
  }
}

timepoint_t to_dvr_time(TimeInfo &t) {//ok
  return timepoint_t {(WORD)t.year, (BYTE)t.month, (BYTE)t.day,
      (BYTE)t.hour, (BYTE)t.minute, (BYTE)t.second};
}

const string log_file_path = "./";

Server * pServer = nullptr;

void Server::InitSDK(){//ok
  pServer = this;
  gt_netsdk_init(log_file_path.c_str());
  auto disconnect_callback = [](gt_dev_handle_t login_handle) -> int {
    auto device = pServer->FindDevice((SESSION_ID)login_handle);
    if(device){
      device->DisConnect();
    }
  };
  if(gt_set_disconnect_callback(disconnect_callback) < 0)
    std::cout<<"Fail to set disconnect callback"<<std::endl;
}

void Server::CleanSDK() {//ok
  gt_netsdk_uninit();
}

void Server::GetVersion(string& _return){//ok
  _return = "1.0.0.5";
}

void Device::Login(){//ok
  long login_id = gt_register_dev(_account.addr.c_str(), _account.port, 0, _account.user.c_str(), _account.password.c_str());
  if (login_id < 0) {
    _client->send_offline(_device_id);
    return;
  }
  _client->send_connected(_device_id);
}

void Device::Logout(){//ok
  gt_unregister_dev((long)_login_id);
}

void Media::StartRealPlay(){//ok
  auto data_callback = [] (gt_session_handle_t real_handle, void * frame_buf, int frame_size,
                           frametype_t frame_type, stream_format_t *format) {
    auto m = pServer->FindMedia((SESSION_ID)real_handle);
    if (m){
      device::info::MediaPackage media;
      media.type = to_media_type(frame_type);
      media.payload = string((char*)frame_buf, (char*)frame_buf + frame_size);
      m->HandleDate(media);
    }else{
      gt_stop_rt_av_service(real_handle);
    }
  };
  auto finsh_callback = [](gt_session_handle_t handle) {
    auto m = pServer->FindMedia((SESSION_ID)handle);
    if(m) m->MediaFinish();
  };

  auto handle = gt_require_rt_av_service((long)_login_id, _play_info.channel, 1, "0.0.0.0", data_callback, finsh_callback);
  if (handle < 0) {
    Log("startplay error: " + get_error_description(gt_get_last_error()));
    MediaFinish();
    return;
  }
  _handle_id = (SESSION_ID)handle;
  _client->send_media_started(_device_id, _media_id);
}

void Media::StopRealPlay(){//ok
  gt_stop_rt_av_service((long)_handle_id);
};

void Media::PlayBackByTime(){
  auto st = to_dvr_time(_play_info.start_time);
  auto et = to_dvr_time(_play_info.end_time);

  auto data_callback = [] (gt_session_handle_t playback_handle, void *frame_buf, int frame_size,
                           frametype_t frame_type, stream_format_t *format) {
    auto m = pServer->FindMedia((SESSION_ID)playback_handle);
    if (m){
      device::info::MediaPackage media;
      media.type = to_media_type(frame_type);
      media.payload = string((char*)frame_buf, (char*)frame_buf + frame_size);
      m->HandleDate(media);
    }else{
      gt_stop_pb_av_service(playback_handle);
    }
  };
  auto finish_callback = [] (gt_session_handle_t playback_handle) {
    auto m = pServer->FindMedia((SESSION_ID)playback_handle);
    if(m) m->MediaFinish();
  };
  auto handle = gt_require_pb_av_service((long)_login_id, _play_info.channel, NSPEED, "0.0.0.0",&st, &et,
                                         data_callback, finish_callback);
  if (handle < 0) {
    MediaFinish();
  }
  _client->send_media_started(_device_id, _media_id);
}

void Media::StopPlayBack(){
  gt_stop_pb_av_service((long)_handle_id);
}
