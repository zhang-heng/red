set(proj dahua_deps)

if(WIN32)
  set(PACKET_URL "http://7viih2.com1.z0.glb.clouddn.com/589_156351_General_NetSDK_Chn_Win32_IS_V3.43.0.R.150602.7z")
else()
  set(PACKET_URL "http://7viih2.com1.z0.glb.clouddn.com/454_167437_General_NetSDK_Chn_Linux32_IS_V3.43.4.R.150629.tar.gz")
  set(${proj}_INC_PATH ${PROJECT_BINARY_DIR}/${proj}/库文件)
  set(${proj}_LIB_PATH ${PROJECT_BINARY_DIR}/${proj}/库文件)
  set(${proj}_LIBS dhnetsdk dhdvr)
endif()

ExternalProject_Add(
  ${proj}
  SOURCE_DIR ${proj}
  URL ${PACKET_URL}
  UPDATE_COMMAND ""
  CONFIGURE_COMMAND ""
  BUILD_COMMAND ""
  INSTALL_COMMAND ""
  #DEPENDS ${proj_DEPENDENCIES}
  )
