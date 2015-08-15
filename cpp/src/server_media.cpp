#include "server_media.h"

Media::Media(Client* client, std::string device_id, SESSION_ID login_id,
			 std::string media_id, const device::info::PlayInfo& play_info):
	_client(client),_device_id(device_id), _login_id(login_id),
	_media_id(media_id), _play_info(play_info){};
