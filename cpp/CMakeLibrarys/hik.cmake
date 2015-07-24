set(proj hik_deps)

if(WIN32)
  set(PACKET_URL "http://download.hikvision.com/UploadFile/SDK/CH-HCNetSDK\(Windows32\)V5.1.1.3.zip")
else()
  set(PACKET_URL "http://download.hikvision.com/UploadFile/SDK/CH-HCNetSDK_V5.0.3.4_build20150114\(for%20Linux32\).zip")
  set(${proj}_INC_PATH ${PROJECT_BINARY_DIR}/${proj}/incCn)
  set(${proj}_LIB_PATH ${PROJECT_BINARY_DIR}/${proj}/lib)
  set(${proj}_LIBS hcnetsdk hpr)
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
