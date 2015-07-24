cmake_minimum_required (VERSION 2.6)
project(netSDK)

## cmake模块
include(ExternalProject)

## 编译参数
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(CMAKE_BUILD_TYPE Release)

## 基础框架


set(BASE_SOURCE_LIST src/base.cpp)
add_library(base ${BASE_SOURCE_LIST})
set(DEPS base)

## hik
include(CMakeLibrarys/hik.cmake)
include_directories(${HIK_DEPS_INC_PATH})
link_directories(${HIK_DEPS_LIB_PATH})
add_executable(hik src/hik.cpp)
target_link_libraries(hik ${DEPS} ${HIK_DEPS_LIBS})

## dahua
include(CMakeLibrarys/dahua.cmake)
add_executable(dahua src/dahua.cpp)
target_link_libraries(dahua ${DEPS})



# if (NOT EXISTS "${MY_DOWNLOAD_PATH}")
    # file(DOWNLOAD "${MY_URL}" "${MY_DOWNLOAD_PATH}")
# endif()

# add_custom_command(
    # OUTPUT "${MY_EXTRACTED_FILE}"
    # COMMAND command to unpack
    # DEPENDS "${MY_DOWNLOAD_PATH}")
