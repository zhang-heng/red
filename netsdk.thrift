namespace java dvr.netsdk
namespace cpp  dvr.netsdk

enum MediaType{
FileHeader  = 1,
MediaData   = 2,
VideoFrame  = 3,
AudioData   = 4,
TalkData    = 5,
PrivatePack = 6,
}

enum eMediaNotify{
Disconnected     = 1,
PlaybackFinished = 2,
}

service NetSdk{
//初始化sdk
bool initSDK(),
//释放SDK并关闭
bool CleanSDK(),
//注册设备（启动报警监听）
bool Login(),
//注销设备
bool Logout(),
/××××××××××维护××××××××××/
//获取设备状态
bool GetStatus(),
//远程升级
bool Updata(),
//重启
bool Restart(),
//设置时间
bool SetTime(),
/××××××××××控制××××××××××/
//云台控制
bool PTZControl(),
//透明串口
bool SerialStart(),
bool SerialSend(),
bool SerialStop(),
/××××××××××预览××××××××××/
//打开预览
bool StartRealPlay(),
//关闭预览
bool StopRealPlay(),
/××××××××××对讲××××××××××/
//启动对讲
bool StartVoiceTalk(),
//发送对讲音频
bool SendVoiceData(),
//关闭对讲
bool StopVoiceTalk(),
/××××××××××点播××××××××××/
//查询录像
bool InquiryRecordTimeSpan(),
//按时间点播
bool PlayBackByTime(),
//按文件名点播
bool PlayBackByFile(),
//停止点播
bool StopPlayBack(),
/×××××回放控制×××××/
//暂停
bool PlayBackPause(),
//恢复
bool PlayBackRestart(),
//快放
bool PlayBackFast(),
//慢放
bool PlayBackSlow(),
//正常速度
bool PlayBackNormalSpeed(),
//改变进度
bool PlayBackSeek(),
/××××××××××下载××××××××××/
//按文件名下载
bool DownloadRecordByFile(),
//按时间下载
bool DownloadRecordByTime(),
//停止下载
bool StopDownload(),
}

service Notify{
//上线通知
oneway void Online(),
//掉线通知
oneway void Offline(),
/××××××××××媒体通知××××××××××/
//媒体数据
oneway void MediaData(1:string session, 2:MediaType type, 3:string data),
//媒体通知
oneway void MediaNotify(1:eMediaNotify what),
/××××××××××报警通知××××××××××/
oneway void AlarmNotify(),
}
