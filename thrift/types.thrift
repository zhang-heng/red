namespace java device.types
namespace cpp  device.types

//媒体请求类型
enum MediaType{
RealPlay,      //实时
VoiceTalk,     //对讲
PlaybackByTime,//按时回放
PlaybackByFile,//按文件回放
}

//媒体负载类型
enum MediaPayloadType{
FileHeader  = 1,//头
MediaData   = 2,//媒体,不区分类型
VideoFrame  = 3,//视频
AudioData   = 4,//音频
TalkData    = 5,//对讲
PrivatePack = 6,//私有,其它
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
