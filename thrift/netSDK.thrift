include "types.thrift"
include "info.thrift"

namespace java device.netsdk
namespace cpp  device.netsdk

const string version = "1.0.0"

//请求操作
service Sdk{
//********初始化库*********/
void InitSDK(),  //初始化sdk
void CleanSDK(), //释放SDK,退出

//**********登入**********/
oneway void Login  (1:info.LoginAccount account, 2:string device_id), //登入
oneway void Logout (1:string device_id),           //登出

//**********维护**********/
string GetVersion(),    //获取版本信息
bool GetStatus(1:string device_id),     //获取设备状态
bool Updata(1:string device_id),        //远程升级
bool Restart(1:string device_id),       //重启
bool SetTime(1:string device_id),       //设置时间
bool ResetPassword(1:string device_id), //重置密码

//**********控制**********/
bool PTZControl(1:string device_id),  //云台控制
bool SerialStart(1:string device_id), //透明串口
bool SerialStop(1:string device_id),
oneway void SerialSend(1:string device_id),

//**********回放**********/
oneway void StartMedia (1:info.PlayInfo play_info, 2:string media_id, 3:string device_id), //启动媒体
oneway void StopMedia  (1:string media_id, 2:string device_id), //停止媒体
void SendMediaData     (1:binary buffer, 2:string media_id, 3:string device_id), //发送对讲音频

//->回放控制
oneway void PlayBackNormalSpeed (1:string media_id, 2:string device_id), //正常速度
oneway void PlayBackPause       (1:string media_id, 2:string device_id), //暂停
oneway void PlayBackFast        (1:string media_id, 2:string device_id), //快放
oneway void PlayBackSlow        (1:string media_id, 2:string device_id), //慢放
oneway void PlayBackSeek        (1:string media_id, 2:string device_id), //改变进度
}

//通知反馈
service Notify{
//**********状态通知**********/
void Lanuched     (1:i32 thrift_port),  //启动完成
void Connected    (1:string device_id), //连接成功
void Offline      (1:string device_id), //断线通知
//**********媒体通知**********/
void MediaStarted (1:string media_id, 2:string device_id), //媒体连接成功
void MediaFinish  (1:string media_id, 2:string device_id), //结束通知
void MediaData    (1:info.MediaPackage data, 2:string media_id, 3:string device_id), //媒体数据
//**********报警通知**********/
void AlarmNotify (),
//************log*************/
void Log(1:string msg),
}
