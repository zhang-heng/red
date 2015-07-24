cmake_minimum_required (VERSION 2.6)
project(netSDK)

## cmake模块
include(ExternalProject)

## 编译参数
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(CMAKE_BUILD_TYPE Release)

## 基础框架
include(CMakeLibrarys/thrift.cmake)

set(BASE_SOURCE_LIST src/base.cpp)
include_directories(${thrift_deps_INC_PATH})
link_directories(${thrift_deps_LIB_PATH})
add_library(base ${BASE_SOURCE_LIST})
target_link_libraries(base ${thrift_deps_LIBS})

set(DEPS base)

## hik
include(CMakeLibrarys/hik.cmake)
include_directories(${hik_deps_INC_PATH})
link_directories(${hik_deps_LIB_PATH})
add_executable(hik src/hik.cpp)
target_link_libraries(hik ${DEPS} ${hik_deps_LIBS})

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
