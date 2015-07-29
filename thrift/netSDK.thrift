namespace java device.netsdk
namespace cpp  device.netsdk

const string version = "1.0.0"

//媒体类型
enum MediaType{
FileHeader  = 1,//头
MediaData   = 2,//媒体,不区分类型
VideoFrame  = 3,//视频
AudioData   = 4,//音频
TalkData    = 5,//对讲
PrivatePack = 6,//私有,其它
}

//媒体包
struct MediaPackage{
1: MediaType  type,
2: i32        reserver,
3: binary     payload,
}

//音频类型
enum VoiceType{
PCM     = 1,
PCMh    = 2,
G711A   = 3,
G711U   = 4,
AAC     = 5,
OGG     = 6,
G723_53 = 7,
G723_63 = 8,
G726    = 9,
G722    = 10,
}

//音频描述
struct VoiceInfo{
1:VoiceType type,        //类型
2:i32       channel,     //通道数
3:i32       bit_ps,      //比特位
4:i32       sample_rate, //采样率
5:i32       block_size,  //块大小
}

//登录信息
struct LoginAccount{
1: string device_id,
2: string addr,
3: i32    port,
4: string user,
5: string password,
}

//码流类型
enum StreamType{
Main = 1, //主码流
Sub  = 2, //辅码流
}

//连接类型
enum ConnectType{
Tcp = 1,
Udp = 2,
}


//媒体请求信息
struct PlayInfo{
1:i32         channel,
2:StreamType  stream_type,
3:ConnectType connect_type,
4:string      start_time,
5:string      end_time
}

//异常
exception InvalidOperation {
  1: i32 what,
  2: string why
}

//请求操作
service Sdk{
void Testing(1: i32 Bps), //测试接口

//********初始化库*********/
bool InitSDK(),  //初始化sdk
bool CleanSDK(), //释放SDK,退出

//**********登入**********/
bool Login  (1:LoginAccount account), //登入
bool Logout (1:string deviceID),      //登出

//**********维护**********/
string xGetVersion(), //获取版本信息
bool xGetStatus(),    //获取设备状态
bool xUpdata(),       //远程升级
bool xRestart(),      //重启
bool xSetTime(),      //设置时间
//**********控制**********/
bool xPTZControl(), //云台控制
bool xSerialStart(), //透明串口
bool xSerialSend(),
bool xSerialStop(),

//**********下载**********/
bool xDownloadRecordByFile(), //按文件名下载
bool xDownloadRecordByTime(), //按时间下载
bool xStopDownload(),         //停止下载

//**********预览**********/
bool StartRealPlay (1:string device_id, 2:PlayInfo play_info), //打开预览
bool StopRealPlay  (1:string device_id, 2:string media_id),    //关闭预览
//**********对讲**********/
bool StartVoiceTalk (1:string device_id, 2:PlayInfo play_info), //启动对讲
oneway void SendVoiceData (1:string media_id, 2:string buffer), //发送对讲音频
bool StopVoiceTalk  (1:string device_id, 2:string media_id),    //关闭对讲
//**********回放**********/
bool PlayBackByTime (1:string device_id, 2:PlayInfo play_info), //按时间点播
bool StopPlayBack   (1:string device_id, 2:string media_id),    //停止点播
//->回放控制
bool PlayBackNormalSpeed (1:string device_id, 2:string media_id), //正常速度
bool PlayBackPause (1:string device_id, 2:string media_id),       //暂停
bool PlayBackFast  (1:string device_id, 2:string media_id),       //快放
bool PlayBackSlow  (1:string device_id, 2:string media_id),       //慢放
bool PlayBackSeek  (1:string device_id, 2:string media_id),       //改变进度
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
oneway void MediaData    (1:string device_id, 2:string media_id, 3:MediaPackage data), //媒体数据
//**********报警通知**********/
oneway void xAlarmNotify (),
//test
oneway void TestBytes(1:binary bytes),
}
