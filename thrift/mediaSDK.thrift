namespace java device.sdk.media
namespace cpp  device.sdk.media

const string version = "1.0.0"

//媒体类型
enum eMediaType{
FileHeader  = 1,//头
MediaData   = 2,//媒体,不区分类型
VideoFrame  = 3,//视频
AudioData   = 4,//音频
TalkData    = 5,//对讲
PrivatePack = 6,//私有,其它
}

//媒体包
struct MediaPackage{
1: string     session
2: eMediaType type,
3: i32        extraType,
4: string     payload,
}

//请求操作
service Sdk{
bool Init(),                     //初始化sdk
bool Uninit(),                   //释放SDK,退出
//**********预览**********/
bool StartRealPlay(),            //打开预览
bool StopRealPlay(),             //关闭预览
//**********对讲**********/
bool StartVoiceTalk(),           //启动对讲
bool SendVoiceData(),            //发送对讲音频
bool StopVoiceTalk(),            //关闭对讲
//**********回放**********/
bool InquiryRecordTimeSpan(),    //查询录像
bool PlayBackByTime(),           //按时间点播
bool PlayBackByFile(),           //按文件名点播
bool StopPlayBack(),             //停止点播
//*****回放控制*****/
bool PlayBackPause(),            //暂停
bool PlayBackRestart(),          //恢复
bool PlayBackFast(),             //快放
bool PlayBackSlow(),             //慢放
bool PlayBackNormalSpeed(),      //正常速度
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
