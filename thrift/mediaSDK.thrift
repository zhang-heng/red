namespace java device.sdk.media
namespace cpp  device.sdk.media

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
1: string     session,
2: MediaType  type,
3: i32        extraType,
4: string     payload,
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
1: string addr,
2: i32    port,
3: string user,
4: string code,
}

//码流类型
enum StreamType{
Main  = 1, //主码流
Sub   = 2, //辅码流
}

//请求操作
service Sdk{
bool Init(),                     //初始化sdk
bool Uninit(),                   //释放SDK,退出

i32  Login(1:LoginAccount a),    //登入
bool Logout(1:i32 loginID),      //登出
//**********预览**********/
i32  StartRealPlay(),            //打开预览
bool StopRealPlay(1:i32 playID), //关闭预览
//**********对讲**********/
bool StartVoiceTalk(),           //启动对讲
bool SendVoiceData(),            //发送对讲音频
bool StopVoiceTalk(),            //关闭对讲
//**********回放**********/
bool PlayBackByTime(),           //按时间点播
bool StopPlayBack(),             //停止点播
//*****回放控制*****/
bool PlayBackPause(),            //暂停
bool PlayBackNormalSpeed(),      //正常速度
bool PlayBackFast(),             //快放
bool PlayBackSlow(),             //慢放
bool PlayBackSeek(),             //改变进度
}

//通知反馈
service Notify{
void Lanuched(),                       //启动完成
void Offline(),                        //断线通知
void MediaFinish(),                    //结束通知
void MediaData(1: MediaPackage data),  //媒体数据

//test
binary TestBytes(1:binary bytes),
}
