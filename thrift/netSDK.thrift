include "types.thrift"
include "info.thrift"

namespace java device.netsdk
namespace cpp  device.netsdk

const string version = "1.0.0"

//请求操作
service Sdk{

//********初始化库*********/
bool InitSDK(),  //初始化sdk
bool CleanSDK(), //释放SDK,退出

//**********登入**********/
bool Login  (1:info.LoginAccount account), //登入
bool Logout (1:string deviceID),           //登出

//**********维护**********/
bool xGetVersion(),    //获取版本信息
bool xGetStatus(),     //获取设备状态
bool xUpdata(),        //远程升级
bool xRestart(),       //重启
bool xSetTime(),       //设置时间
bool xResetPassword(), //重置密码

//**********控制**********/
bool xPTZControl(),  //云台控制
bool xSerialStart(), //透明串口
bool xSerialStop(),
oneway void xSerialSend(),

//**********下载**********/
bool xDownloadRecordByFile(), //按文件名下载
bool xDownloadRecordByTime(), //按时间下载
bool xStopDownload(),         //停止下载

//**********预览**********/
bool StartRealPlay (1:string device_id, 2:string media_id, 3:info.PlayInfo play_info), //打开预览
bool StopRealPlay  (1:string device_id, 2:string media_id),     //关闭预览

//**********对讲**********/
bool StartVoiceTalk (1:string device_id, 2:string media_id, 3:info.PlayInfo play_info), //启动对讲
bool StopVoiceTalk  (1:string device_id, 2:string media_id),    //关闭对讲
oneway void SendVoiceData (1:string media_id, 2:string buffer), //发送对讲音频

//**********回放**********/
bool PlayBackByTime (1:string device_id, 2:string media_id, 3:info.PlayInfo play_info), //按时间点播
bool StopPlayBack   (1:string device_id, 2:string media_id), //停止点播
//->回放控制
bool PlayBackNormalSpeed (1:string device_id, 2:string media_id), //正常速度
bool PlayBackPause       (1:string device_id, 2:string media_id), //暂停
bool PlayBackFast        (1:string device_id, 2:string media_id), //快放
bool PlayBackSlow        (1:string device_id, 2:string media_id), //慢放
bool PlayBackSeek        (1:string device_id, 2:string media_id), //改变进度
}




//通知反馈
service Notify{
//**********状态通知**********/
oneway void Lanuched(), //启动完成
oneway void Connected    (1:string device_id), //连接成功
oneway void Offline      (1:string device_id), //断线通知
//**********媒体通知**********/
oneway void MediaStarted (1:string device_id, 2:string media_id), //媒体连接成功
oneway void MediaFinish  (1:string device_id, 2:string media_id), //结束通知
oneway void MediaData    (1:string device_id, 2:string media_id, 3:info.MediaPackage data), //媒体数据
//**********报警通知**********/
oneway void xAlarmNotify (),
//test
oneway void TestBytes(1:binary bytes),
}
