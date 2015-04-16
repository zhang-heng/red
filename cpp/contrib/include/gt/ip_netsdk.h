/**  @file	ip_netsdk_ip.h
 *   @brief ipvsϵýͥ·�Ĺ݀쭥tsdk£¬°�拽¾ޓ쑅®½»»¥£¬вʏ²䍨¹�»»¥
 *   @date 	2014.03
 */

/*	ChangeLog
 *	1.0.0.5 add gps support and fix a cmd connection bug
 *	1.0.0.4 support old dev direct mode! merge .hs
 *	1.0.0.3 信令部分添加设备在线心跳，目前只通过8095 sniff
 *	1.0.0.2 信令部分功能添加，目前只支持状态、报警等
 *	1.0.0.1 ip_netsdk c++版本，实时媒体改为被动连接
*/

#ifndef IP_NETSDK_IP_H
#define	IP_NETSDK_IP_H
#ifdef _WIN32
#include <winsock.h>
#else
#include <sys/time.h>
//#include <typedefine.h>
#define BYTE    unsigned char
#define WORD    unsigned short
#define DWORD   unsigned long
#endif
//#include <ip_netsdk_errno.h>
//#include <ip_netsdk_exdef.h>
//#include <gtsf.h>

#undef IN
#undef OUT
#undef IO

#define	IN
#define	OUT
#define IO

#ifndef BYTE
#define BYTE	unsigned char
#endif
#ifndef LONG
#define LONG	long
#endif
#define CBL(x,y)	{x,#x,y}
#ifdef __cplusplus
extern "C" {
#endif

#ifdef _WIN32
  //windows
#define EXPORT_DLL __declspec(dllexport)
#else
  //linux
#define EXPORT_DLL
#define CALLBACK
#endif
///////////////////////////--macro definition--/////////////////////////////////////////

//由嵌入式设备发出的错误码
#define RESULT_SUCCESS			0	//成功
#define ERR_DVC_INTERNAL		0x1001	//设备内部错
#define ERR_DVC_INVALID_REQ		0x1002	//客户请求数据格式错
#define ERR_DVC_BUSY			0X1003	//设备忙
#define ERR_DVC_FAILURE			0x1004  //设备故障
#define ERR_EVC_CRC_ERR			0x1005	//设备收到一个crc错误的数据包
#define ERR_EVC_NOT_SUPPORT		0x1006  //设备收到一个不支持的命令
#define ERR_ENC_NOT_ALLOW		0x1007  //设备收到一个不允许的命令
#define ERR_DVC_NO_RECORD_INDEX	0x1008	//设备没有查询到索引
//升级相关的错误码 
#define ERR_DVC_INVALID_NAME	0x1010  //升级文件名字格式错误
#define ERR_DVC_LOGIN_FTP    	0x1011  //无法登录ftp服务器
#define ERR_DVC_NO_FILE      	0x1012  //ftp服务器上无指定的文件或用户对其无读权限
#define ERR_DVC_UNTAR        	0x1013  //解压文件失败
#define ERR_NO_SPACE         	0x1014  //设备存储空间不够，无法升级
#define ERR_DVC_PKT_NO_MATCH	0x1015	//升级包与设备型号不匹配
#define ERR_DVC_UPDATE_FILE		0x1016	//更新设备文件(证书，配置文件)错误
#define ERR_DVC_WRONG_SIZE		0x1017	//文件大小不符
///网络相关错误
#define ERR_SDK_ENETDOWN        	(ERR_SDK_ERRNO_BASE + 100)        ///<Network is down  
#define ERR_SDK_ENETUNREACH     	(ERR_SDK_ERRNO_BASE + 101)        ///<Network is unreachable 网络不可达
#define ERR_SDK_ENETRESET       	(ERR_SDK_ERRNO_BASE + 102)       ///<Network dropped connection because of reset	连接断开
#define ERR_SDK_ECONNABORTED    	(ERR_SDK_ERRNO_BASE + 103)       ///<Software caused connection abort	放弃连接
#define ERR_SDK_ECONNRESET      	(ERR_SDK_ERRNO_BASE + 104)       ///<Connection reset by peer 连接复位
#define ERR_SDK_ENOBUFS         	(ERR_SDK_ERRNO_BASE + 105)       ///<No buffer space available	缓冲区不足
#define ERR_SDK_EISCONN         	(ERR_SDK_ERRNO_BASE + 106)       ///<Transport endpoint is already connected 连接已建立
#define ERR_SDK_ENOTCONN        	(ERR_SDK_ERRNO_BASE + 107)       ///<Transport endpoint is not connected 连接未建立
#define ERR_SDK_ESHUTDOWN       	(ERR_SDK_ERRNO_BASE + 108)       ///<Cannot send after transport endpoint shutdown 对方连接断开,不能发送
#define ERR_SDK_ETOOMANYREFS    	(ERR_SDK_ERRNO_BASE + 109)       ///<Too many references: cannot splice 
#define ERR_SDK_ETIMEDOUT       	(ERR_SDK_ERRNO_BASE + 110)       ///<Connection timed out	连接超时
#define ERR_SDK_ECONNREFUSED    	(ERR_SDK_ERRNO_BASE + 111)       ///<Connection refused		连接被拒绝

//NETSDK 相关错误码
#define	ERR_SDK_ERRNO_BASE		0x2000	///错误码基数
#define	ERR_SDK_EPERM			(ERR_SDK_ERRNO_BASE + 1)	///<Operation not permitted(不允许的操作)
#define	ERR_SDK_ENOENT			(ERR_SDK_ERRNO_BASE + 2)	///<No such file or directory(没有指定的文件或目录)
#define ERR_SDK_EBUSY			(ERR_SDK_ERRNO_BASE + 3)	///<Device or resource busy(设备或资源忙)
#define ERR_SDK_EINVAL			(ERR_SDK_ERRNO_BASE + 4)	///<Invalid argument(参数错误)
#define ERR_SDK_EMFILE			(ERR_SDK_ERRNO_BASE + 5)	///<Too many open files(打开的文件过多)
#define ERR_SDK_EAGAIN			(ERR_SDK_ERRNO_BASE + 6)	///<Try again(稍后重试)
#define ERR_SDK_ENOMEM			(ERR_SDK_ERRNO_BASE + 7)	///<Out of memory(内存不足)
#define ERR_SDK_EFBIG           (ERR_SDK_ERRNO_BASE + 8)    ///<File too large (文件过大)
#define ERR_SDK_UNKNOW			(ERR_SDK_ERRNO_BASE + 9)	///<unknow error(未知错误)
#define ERR_SDK_ECFILE			(ERR_SDK_ERRNO_BASE + 10)	//创建文件失败
#define ERR_SDK_UNKNOW_CERT		(ERR_SDK_ERRNO_BASE	+ 11)	//不支持的证书格式
#define ERR_SDK_OP_TIMEOUT		(ERR_SDK_ERRNO_BASE	+ 12)	//操作超时
#define ERR_SDK_NOT_SUPPORT		(ERR_SDK_ERRNO_BASE	+ 13)	//SDK不支持的操作


#define MEDIA_VIDEO		  0x01	//视频数据
#define MEDIA_AUDIO	    0x02	//音频数据
#define MEDIA_SPEECH		0x03	//对讲数据
#define MEDIA_COMMAND		0x04	//信令，在发送命令用
#define MEDIA_STREAMID	0x05	//流票,由TCP监听方产生，主动连接方在连接后发送给监听方
#define MEDIA_AVIHEAD   0x06

#define FRAMETYPE_I		0x0		// IME6410 Header - I Frame
#define FRAMETYPE_P		0x1		// IME6410 Header - P Frame
#define FRAMETYPE_B		0x2
#define FRAMETYPE_PCM	0x5		// IME6410 Header - Audio Frame
#define FRAMETYPE_ADPCM	0x6		// IME6410 Header - Audio Frame
#define FRAMETYPE_MD	0x8

#define RATIO_D1_PAL       0x0      //720*576
#define RATIO_D1_NTSC      0x1      //704*576
#define RATIO_CIF_PAL      0x2      // 352*288
#define RATIO_CIF_NTSC     0x3      // 320*240

//视频编码类型
#define         VIDEO_MPEG4                     0
#define         VIDEO_H264                      1
#define         VIDEO_MJPEG                     2


#define         AUDIO_PCMU                      7
#define         AUDIO_AAC                       255

//////////////////////--end-macro--///////////////////////////////////////////

//////////////////////--typedef--////////////////////////////////////////////

typedef struct{	//视频格式信息结构
	struct timeval tv;			//数据产生时的时间戳
	unsigned long	Sequence;  //序列号
	unsigned char format;		//编码格式format
	unsigned char  type;		//frame type	I/P/B...
	unsigned char ratio;  //分辨率
	unsigned char recv;			//是否是pal制视频
}stream_video_format_t;

typedef struct{	//音频格式信息结构
	struct timeval tv;
	unsigned short a_sampling;	//声音采样率
	unsigned char  a_wformat;	//声音格式
	unsigned char  a_channel;	//声音通道
	unsigned char  a_nr_frame;	//一包声音里面有几块数据
	unsigned char  a_bitrate;		//声音码流
	unsigned char  a_bits;		//音频采样位数
	unsigned char  a_frate;		//音频数据的帧率(没秒钟有几包音频数据)
	//unsigned char  reserve;
}stream_audio_format_t;

typedef union{ //媒体格式定义联合体
	stream_video_format_t v_fmt;
	stream_audio_format_t a_fmt;
}stream_format_t;

typedef struct _SYSTEMTIME {
	WORD wYear;
	WORD wMonth;
	WORD wDayOfWeek;
	WORD wDay;
	WORD wHour;
	WORD wMinute;
	WORD wSecond;
	WORD wMilliseconds;
} SYSTEMTIME, *PSYSTEMTIME, *LPSYSTEMTIME;

typedef struct{///设备的注册信息
		BYTE	dev_guid[8];			// 8字节的guid
		BYTE	dev_guid_str[128];		// guid的字符串形式

        DWORD vendor;                   //设备制造商标识(4) +设备型号标识(4)
        DWORD device_type;              //设备型号      
        BYTE site_name[40];             //安装地点名称  

        WORD video_num;					//设备视频输入通道数。
        WORD com_num;                   // 串口数
        WORD storage_type;              //设备的存储介质类型 0：没有 1：cf卡 2：硬盘
        DWORD storage_room;				//设备的存储介质容量 单位Mbyte
        WORD compress_num;			    //压缩通道数    目前为1，2或5
        WORD compress_type;				//压缩数据类型，(压缩芯片，压缩格式)
        WORD audio_compress;			//声音输入压缩类型
        WORD audio_in;                  //声音输入通道数，目前为1
        WORD switch_in_num;             //开关量输入通道数
        WORD switch_out_num;			//开关量输出通道数
       
		DWORD    cmd_port;              //命令服务端口
        DWORD    image_port;            //图像服务端口  
        DWORD    audio_port;            //音频服务端口
		DWORD    pb_port;               //录像点播服务端口
        BYTE    firmware[20];           //固件版本号，暂时不用
        BYTE    dev_info[40];           //设备的一些相关信息
        BYTE    ex_info[160];           //外接dvs(如果有的话)的相关信息，包括品牌，端口，用户名，密码
		BYTE    video_names[64][30];    //最多64路通道名称
}dev_regist_info_t;
 
typedef struct{
	BYTE  dev_id[8];      //设备id                                           
	WORD  state;        //状态；0：正常 1：无gps信号 2：正在定位 3:故障
	BYTE  reserved[6];      //保留                               
	double  lon;        //经度                                      
	double  lat;        //纬度                                       
	double  direction;      //方位角                                   
	double  altitude;     //海拔高度                                 
	double  speed;        //速度(km/h)
}gps_info_t;

typedef struct{
	int audio_head;
	int audio_length;
	char data[0];
}audio_t;

#define audio_head 0x77061600

typedef enum
{
	AD_PCMU = 5,
	AD_AAC
}ad_t;

typedef enum
{
	QSPEED = 0,
	HSPEED,
	NSPEED,
	DSPEED,
	ESPEED,
	ISPEED
}speed_t;

typedef struct timepoint
{
	WORD year;
	BYTE month;
	BYTE day;
	BYTE hour;
	BYTE minute;
	BYTE second;
	BYTE reserve;
}timepoint_t;

  //handle
typedef long gt_dev_handle_t;               //cmd
typedef long	 gt_session_handle_t;           //media
  
typedef enum{
    FRAMETYPE_V = 1,          ///<  video
    FRAMETYPE_A,              ///<  audio
    FRAMETYPE_H,
}frametype_t;
  
typedef enum{
 	ALARM_STATE = 1,
 	ERROR_STATE,
 	TRIG_INFO,
	GPS_INFO,
}cmd_type_t;

typedef enum record_ctrlcmd
{
	CPAUSE = 0,
	CRESUME,
	CSPEED,
	CSEEK,
} record_ctrlcmd_t;

typedef struct
{
	record_ctrlcmd_t ctrl;
	int              speed;
	timepoint_t      start;
	timepoint_t      end;
} record_ctl_t;

/**
   * @brief     实时回调
   * @param     real_handle 调用gt_require_rt_av_service获得的实时播放句柄
   * @param     frame_buf   帧数据
   * @param     len         帧数据长度
   * @param     type        帧数据类型
   * @return
   */
  typedef void (CALLBACK *frame_data_Callback)(gt_session_handle_t real_handle, void *frame_buf, int len,frametype_t vflag,stream_format_t* format);

  /**
   * @brief     录像回放结束回调
   * @param     playback_handle  调用gt_require_pb_av_service得到的录像回放句柄
   */
  typedef void (CALLBACK *finish_Callback)(gt_session_handle_t playback_handle);


  /**
   * @brief     注册网关回调
   * @param     login_handle     设备注册后返回的登录句柄
   */
	typedef int (CALLBACK *Devregist_Callback)(gt_dev_handle_t login_handle);

  /**
   * @brief     设备断线回调
   * @param     login_handle     设备注册后返回的登录句柄
   */
	typedef int (CALLBACK *Devdisconnect_Callback)(gt_dev_handle_t login_handle);

  /**
   * @brief     设备断线回调
   * @param     login_handle     设备注册后返回的登录句柄 cmdtype命令类型 cmd命令明文 cmd_len 命令长度
   */
	typedef int (CALLBACK *Devcmd_Callback)(gt_dev_handle_t login_handle,cmd_type_t cmdtype,const BYTE* cmd,int cmd_len);
	
//////////////////////////--end-typedef--///////////////////////////////////


//////////////////////////--interface--/////////////////////////////////////
  
  /**
   * @brief     初始化netsdk，一个运行实例中调用1次
   * @param     none
   * @return	0:初始化成功，<0出错
   */
  EXPORT_DLL int gt_netsdk_init(const char* logpath);

  /**
   * @brief    去初始化netsdk
   * @param    none
   * @return   0:去初始化成功，<0出错
   */
  EXPORT_DLL int gt_netsdk_uninit(void);
  
  /**
   *   @brief     开启接受被动注册功能
   *   @param     gate_port  监听注册端口号, reg_callback，设备注册后回调接口 cmd_callback，设备信令回调接口
   *   @return    0表示成功，负值表示失败
   */ 
  EXPORT_DLL int gt_open_gateway(IN int gate_port,IN Devregist_Callback reg_callback);
  
  /**
   *   @brief     设置设备断线回调
   *   @param     disconnect_callback 掉线回调函数
   *   @return    0表示成功，负值表示失败
   */ 
  EXPORT_DLL int gt_set_disconnect_callback(IN Devdisconnect_Callback disconnect_callback);

  /**
   *   @brief     设置设备命令回调
   *   @param     login_handle 设备登录handle, disconnect_callback 掉线回调函数
   *   @return    0表示成功，负值表示失败
   */ 
  EXPORT_DLL int gt_set_cmd_callback(IN Devcmd_Callback cmd_callback);
	
  /**
   * @brief    登录到远程设备,创建设备描述结构
   * @param    dev_ip     设备ip字符串
   * @param    dev_port   设备命令端口号
   * @param    env        是否使用数字证书 1:使用 0:不使用
   * @param    usrname:
   *               当env=0时表示用户名字符串 NULL表示不需要用户名
   *               当env=1时表示数字证书公钥文件名
   * @param    passwd:
   *               当env=0时表示密码字符串 NULL表示不需要密码
   *               当env=1时表示数字证书私钥文件名
   * @return   指向远程设备的句柄指针,NULL表示失败，进行对设备操作时直接使用这个句柄；
   * 注意:env=1时 usrname和passwd不能为空
   */
  EXPORT_DLL gt_dev_handle_t gt_register_dev(IN const char *dev_ip, IN int dev_port, IN int env,
                                             IN const char *usrname, IN const char *passwd);

  /**
   * @brief  将 login_handle 销毁相关结构, 必须在所有相关链接断开后调用
   */
  EXPORT_DLL int gt_unregister_dev(IN gt_dev_handle_t login_handle);

  /**
   * @brief      异步请求实时视频，将对应handle加入传输管理链表
   * @parm       login_handle 调用gt_register_dev获得的设备句柄
   * @parm       channel      请求通道号,
   * @parm       audioenable  是否需要音频
   * @parm       head_buf     返回的avi头,
   * @parm       head_len     返回的avi头长度
   * @parm       callback     每一帧的回调函数,Rtframe_Callback类型
   * @parm       mode         实时模式，默认为0：主动 1：被动
   * @return     返回实时播放的real_handle值，为NULL表示失败
   */
  EXPORT_DLL gt_session_handle_t gt_require_rt_av_service(IN gt_dev_handle_t login_handle, IN int channel, IN int audioenable,IN const char* local_ip, IN frame_data_Callback callback,IN finish_Callback fini_callback,int mode=0);

  /**
   * @brief     停止实时视频，从传输管理数组删除
   * @parm      real_handle  调用gt_require_rt_av_service获得的实时播放句柄
   * @return    是否成功停止请求，0正常 <0 异常
   */
  EXPORT_DLL int gt_stop_rt_av_service(gt_session_handle_t real_handle,int mode=0);
 
  /**
   * @brief       请求录像视频，将对应handle加入传输管理链表
   * @param       login_handle   调用gt_register_dev得到的连接句柄
   * @param       channel        请求通道号,speed播放速率,start/end播放时间段
   * @param       speed          回放速率，但是取值范围呢？
   * @param       start          回放起始时间
   * @param       end            回放结束时间
   * @param       callback       帧数据回调函数,Pbframe_Callback类型
   * @param       fini_callback  回放结束回调, Pbfinish_Callback类型
   * @return      该设备录像请求的playback_handle值，等于NULL为失败
   */
  EXPORT_DLL gt_session_handle_t gt_require_pb_av_service(IN gt_dev_handle_t login_handle, IN int channel, IN int speed,IN const char* local_ip, IN timepoint_t* start, IN timepoint_t* end, IN frame_data_Callback callback, IN finish_Callback fini_callback,int mode=0);

  /**
   * @brief     停止事实视频，从传输管理链表中删除
   * @parm      playback_handle  调用gt_require_pb_av_service获取的录像回放句柄
   * @return    是否成功停止请求，0正常 <0 异常
   */
  EXPORT_DLL int gt_stop_pb_av_service(IN gt_session_handle_t playback_handle,int mode=0);

    /**
   * @brief    录像播放控制
   * @parm     playback_handle   调用gt_require_pb_av_service得到的录像回放句柄
   * @parm     cmd               record_ctrlcmd类型，停止，继续，速度，定位等
   * @return   是否成功停止请求，0正常 <0 异常
   */
  EXPORT_DLL int gt_ctrl_pb_av_service(IN gt_session_handle_t playback_handle, IN record_ctl_t* cmd);

  /**
   * @brief    查询录像索引，返回录像索引列表
   * @parm     login_handle   调用gt_register_dev得到的连接句柄
   * @parm     start          起始时间
   * @parm     end            结束时间
   * @parm     channel        通道号
   * @parm     index_path     存放传出的录像索引路径
   * @parm     name_buf_len   传入数组长度
   * @return   是否有录像，0正常 <0 没有
   */
  EXPORT_DLL int gt_query_ftp_record(IN gt_dev_handle_t login_handle, IN timepoint_t *start, IN timepoint_t *end, IN int channel, OUT char *index_path, IN int name_buf_len);

  /**
   *   @brief     获取最后一次发生错误的错误码
   *   @param     无
   *   @return    最后一次发生错误的错误码
   */
  EXPORT_DLL int gt_get_last_error(void);

  /**
   *   @brief     设置tcp连接超时时间(全局)
   *   @param     timeout  判断超时的时间(秒)，默认值10s
   *   @return    无
   *              应用程序如果需要改变默认值则启动时调用一次即可
   */
  EXPORT_DLL void gt_set_connect_timeout(IN int timeout);

  /**
   *   @brief     获取tcp连接超时时间(全局)
   *   @param     无
   *   @return    timeout 判断超时的时间(秒)
   */
  EXPORT_DLL int gt_get_connect_timeout(void);

  /**
   *   @brief    获取设备的注册信息
   *   @param    login_handle  调用gt_register_dev得到的连接句柄;
   *   @param    info          存放设备相关信息
   *   @return   0表示成功,负值表示失败
   */
  EXPORT_DLL int gt_query_regist_info(IN gt_dev_handle_t login_handle, OUT dev_regist_info_t *info);

  /**
   *   @brief     开始订阅音频下行服务
   *   @param	  login_handle  调用gt_register_dev得到的连接句柄
   *   @param     speak_port    设备端的音频连接端口 8097
   *   @param     channel       下行音频通道号
   *   @param     encoder       编码方式，现取1
   *   @param     sample_rate   音频数据采样率
   *   @return    返回下行语音的speak handle值, 为NULL表示失败
   */
  EXPORT_DLL gt_session_handle_t gt_require_speak_service(IN gt_dev_handle_t login_handle, IN int channel, IN int speak_port, IN int encoder, IN int sample_rate);

  /**
   *   @brief   向设备发送下行音频数据
   *   @param   speak_handle  调用gt_register_dev得到的连接句柄
   *   @param   frame_buf     音频数据缓冲区buf_len音频数据长度
   *   @return	负值表示出错,正值表示发送数据长度
   */
  EXPORT_DLL int gt_write_speak_data(IO gt_session_handle_t speak_handle, IN BYTE *frame_buf, IN int buf_len);

  /**
   *   @brief     停止订阅音频下行服务
   *   @param     speak_handle  调用gt_require_speak_service得到的连接句柄
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_stop_speak_service(IO gt_session_handle_t speak_handle);
  
  /**
   *   @brief     查询设备状态
   *   @param     login_handle  设备注册句柄 state设备状态（按位表示）
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_query_device_state(IN gt_dev_handle_t login_handle,OUT DWORD* state,OUT int* statelen);
  
  /**
   *   @brief     报警确认
   *   @param     login_handle  设备注册句柄 confirm报警确认通道（按位表示）
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_send_alarm_confirm(IN gt_dev_handle_t login_handle,IN DWORD confirm);
  
  /**
   *   @brief     报警取消
   *   @param     login_handle  设备注册句柄 cancel报警取消通道（按位表示）
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_send_alarm_cancel(IN gt_dev_handle_t login_handle,IN DWORD cancel);
  
  /**
   *   @brief     设置透明串口参数
   *   @param     login_handle  设备注册句柄 serial_ch串口通道号
   								baud          波特率
   								databit       数据位 一般为8  					
   								parity        奇偶校验位 一般不需要为'N'   								
   								stopbit       停止位 一般为1
   								flow          流量控制 一般不需要
   								save_flag     是否保存到配置中
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_set_tranpass_serial(IN gt_dev_handle_t login_handle,IN int serial_ch,IN DWORD baud,IN BYTE databit,IN BYTE parity,IN BYTE stopbit,IN BYTE flow,IN int save_flag);
  
  /**
   *   @brief     打开透明串口
   *   @param     login_handle  设备注册句柄 serial_ch串口通道号   								
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_open_tranpass_serial(IN gt_dev_handle_t login_handle,IN int serial_ch);
  
	/**
   *   @brief     发送透明串口数据
   *   @param     login_handle  设备注册句柄 serial_ch串口通道号
   								data_buf 数据 data_len 数据长度   								
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_send_tranpass_serial(IN gt_dev_handle_t login_handle,IN int serial_ch,IN const BYTE* data_buf,IN int data_len);
  
  /**
   *   @brief     接收透明串口数据
   *   @param     login_handle  设备注册句柄 serial_ch串口通道号
   								data_buf 数据 data_len 数据长度   								
   *   @return    大于等于0表示接收数据，小于0表示失败
   */
  EXPORT_DLL int gt_recv_tranpass_serial(IN gt_dev_handle_t login_handle,IN int serial_ch,IN const BYTE* data_buf,IN int* data_len);
  
  /**
   *   @brief     关闭透明串口
   *   @param     login_handle  设备注册句柄 serial_ch串口通道号	
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_close_tranpass_serial(IN gt_dev_handle_t login_handle,IN int serial_ch);
  
  /**
   *   @brief     open gps     
   *   @param     login_handle  设备注册句柄 interval : the frequence of sending , duration:how long you wanna get gps data
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_open_gps(IN gt_dev_handle_t login_handle,IN int interval,IN int duration);

  /**
   *   @brief     关闭gps
   *   @param     login_handle  设备注册句柄 
   *   @return    0表示成功，负值表示失败
   */
  EXPORT_DLL int gt_close_gps(IN gt_dev_handle_t login_handle);
#ifdef __cplusplus
} // extern "C"
#endif


#endif
