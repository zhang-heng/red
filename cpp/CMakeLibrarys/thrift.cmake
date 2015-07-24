set(proj thrift_deps)
set(PACKET_URL "https://git-wip-us.apache.org/repos/asf/thrift.git")

set(${proj}_INC_PATH ${PROJECT_BINARY_DIR}/${proj}/lib/cpp/src ${PROJECT_BINARY_DIR}/${proj}-prefix/src/${proj}-build)
set(${proj}_LIB_PATH ${PROJECT_BINARY_DIR}/${proj}-prefix/src/${proj}-build/lib)
set(${proj}_LIBS thrift)

ExternalProject_Add(
  ${proj}
  SOURCE_DIR ${proj}
  GIT_REPOSITORY ${PACKET_URL}
  GIT_TAG master
  UPDATE_COMMAND ""
  #  CONFIGURE_COMMAND cd ${PROJECT_BINARY_DIR}/${proj}/lib/cpp
  #  BUILD_COMMAND ""
  INSTALL_COMMAND ""
  )
