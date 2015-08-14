include "types.thrift"

namespace java device.info
namespace cpp  device.info

//设备信息
struct DeviceInfo{
1:string serial_number,    //设备序列号
2:i32 n_alarm_in,          //报警输入端子个数
3:i32 n_alarm_out,         //报警输出端子个数
4:string device_model,     //设备型号
5:i32 n_an_video_channels,      //模拟视频通道数
6:i32 n_audio_channels,         //音频通道数
7:i32 start_an_video_channel,   //模拟首通道数
8:i32 n_dig_video_channels,     //数字通道数
9:i32 start_dig_video_channels, //数字首通道
10:i32 n_talk_channels,         //对讲通道数
11:i32 start_talk_channels,     //对讲首通道
12:i32 n_com,                   //串口个数
13:i32 n_disk,                  //硬盘个数
}

//媒体包
struct MediaPackage{
1: types.MediaType  type,
2: i32              reserver,
3: binary           payload,
}

//音频描述
struct VoiceInfo{
1:types.VoiceType type,        //类型
2:i32             channel,     //通道数
3:i32             bit_ps,      //比特位
4:i32             sample_rate, //采样率
5:i32             block_size,  //块大小
}

//登录信息
struct LoginAccount{
1: string addr,
2: i32    port,
3: string user,
4: string password,
}

//媒体请求信息
struct PlayInfo{
1:i32               channel,
2:types.StreamType  stream_type,
3:types.ConnectType connect_type,
4:string            start_time,
5:string            end_time
}

//异常
exception InvalidOperation {
1: i32    what,
2: string why
}
