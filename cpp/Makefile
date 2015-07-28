OBJ_DIR = .obj

COMPILE = g++ -O2 -s -std=c++11 -m32 -pthread
TARGET = ../bin/sdk

all: hik.exe dahua.exe

clean:
	rm -rf $(OBJ_DIR)
	rm -rf $(TARGET)

#*********libs*********
libs_path = ../third-part/linux

thrift_include_path = $(libs_path)/thrift
thrift_links_path = -L$(libs_path)/thrift
thrift_links = -lthrift

#*******thrift_gen*******
thrift_gen_src_path = ../thrift/gen-cpp
thrift_gen_srcs = $(filter-out $(wildcard $(thrift_gen_src_path)/*.skeleton.cpp), $(wildcard $(thrift_gen_src_path)/*.cpp))
thrfit_gen_obj_path = $(OBJ_DIR)/thrift_gen
thrift_gen_objs = $(patsubst %.cpp, $(thrfit_gen_obj_path)/%.o, $(notdir $(thrift_gen_srcs)))
$(shell mkdir -p $(thrfit_gen_obj_path))
$(thrfit_gen_obj_path)/%.o : $(thrift_gen_src_path)/%.cpp
	$(COMPILE) -I $(thrift_include_path) -c -o $@ $<

#**********base**********
base_src_path = ./src
base_srcs = $(wildcard $(base_src_path)/*.cpp)
base_obj_path = $(OBJ_DIR)/base
base_objs = $(thrift_gen_objs) $(patsubst %.cpp, $(base_obj_path)/%.o, $(notdir $(base_srcs)))
base_links = $(thrift_links)
$(shell mkdir -p $(base_obj_path))
$(base_obj_path)/%.o : $(base_src_path)/%.cpp
	$(COMPILE) -I $(thrift_include_path) -I $(thrift_gen_src_path) -c -o $@ $<

#**********hik************
hik_links_path = -L$(libs_path)/hik $(thrift_links_path)
hik_links = $(base_links) -lhcnetsdk -lHCCore -lhpr
$(shell mkdir -p $(TARGET)/hik)
hik.exe :$(base_objs)
	$(COMPILE) src/sdks/hik.cpp $(base_objs) -I $(libs_path)/hik $(hik_links_path) $(hik_links) -o $(TARGET)/hik/hik.exe

#**********dahua**********
dahua_links_path = -L$(libs_path)/dahua $(thrift_links_path)
dahua_links = $(base_links) -ldhnetsdk -ldhdvr
$(shell mkdir -p $(TARGET)/dahua)
dahua.exe :$(base_objs)
	$(COMPILE) src/sdks/dahua.cpp $(base_objs) -I $(libs_path)/dahua $(dahua_links_path) $(dahua_links) -o $(TARGET)/dahua/dahua.exe
