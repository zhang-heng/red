## cmake
cmake_minimum_required (VERSION 2.6)
project(netSDK)
include(ExternalProject)

## 编译参数
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(CMAKE_BUILD_TYPE Release)

## 基础框架
include(CMakeLibrarys/thrift.cmake)
set(THRIFT_GEN_PATH ${PROJECT_SOURCE_DIR}/../thrift/gen-cpp)
AUX_SOURCE_DIRECTORY(${THRIFT_GEN_PATH} THRIFT-GEN-SOURCES)
AUX_SOURCE_DIRECTORY(src BASE_SOURCE_LIST)

include_directories(${thrift_deps_INC_PATH} ${THRIFT_GEN_PATH})
link_directories(${thrift_deps_LIB_PATH})

add_library(base ${BASE_SOURCE_LIST} ${THRIFT-GEN-SOURCES})
target_link_libraries(base ${thrift_deps_LIBS})

## hik
include(CMakeLibrarys/hik.cmake)
include_directories(${hik_deps_INC_PATH})
link_directories(${hik_deps_LIB_PATH})
add_executable(hik src/sdks/hik.cpp)
target_link_libraries(hik base ${hik_deps_LIBS})

## dahua
include(CMakeLibrarys/dahua.cmake)
include_directories(${dahua_deps_INC_PATH})
link_directories(${dahua_deps_LIB_PATH})
add_executable(dahua src/sdks/dahua.cpp)
target_link_libraries(dahua base ${dahua_deps_LIBS})
