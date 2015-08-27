OBJ_DIR = .obj

COMPILE = g++ -O2 -s -std=c++11 -m32 -pthread
TARGET = ../clojure/resources/sdk

all: hik.exe dahua.exe gt.exe xm.exe

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

#**********main**********
main_src_path = src
main_srcs = $(wildcard $(main_src_path)/*.cpp)
main_include = -I $(thrift_include_path) -I $(thrift_gen_src_path)
main_obj_path = $(OBJ_DIR)/main
main_objs = $(thrift_gen_objs) $(patsubst %.cpp, $(main_obj_path)/%.o, $(notdir $(main_srcs)))
main_links = $(thrift_links)
$(shell mkdir -p $(main_obj_path))
$(main_obj_path)/%.o : $(main_src_path)/%.cpp
	$(COMPILE) $(main_include) -c -o $@ $<

#**********hik************
hik_include = -I $(libs_path)/hik $(main_include)
hik_links_path = -L$(libs_path)/hik $(thrift_links_path)
hik_links = $(main_links) -lhcnetsdk -lhpr
$(shell mkdir -p $(TARGET)/hik)
hik.exe :$(main_objs)
	$(COMPILE) src/sdks/hik.cpp $(main_objs) $(hik_include) $(hik_links_path) $(hik_links) -o $(TARGET)/hik/hik.exe
	$(shell cp $(libs_path)/hik/*.so $(TARGET)/hik/)

#**********dahua**********
dahua_include = -I $(libs_path)/dahua $(main_include)
dahua_links_path = -L$(libs_path)/dahua $(thrift_links_path)
dahua_links = $(main_links) -ldhnetsdk -ldhdvr
$(shell mkdir -p $(TARGET)/dahua)
dahua.exe :$(main_objs)
	$(COMPILE) src/sdks/dahua.cpp $(main_objs) $(dahua_include) $(dahua_links_path) $(dahua_links) -o $(TARGET)/dahua/dahua.exe
	$(shell cp $(libs_path)/dahua/*.so $(TARGET)/dahua/)

#**********gt**********
gt_include = -I $(libs_path)/gt $(main_include)
gt_links_path = -L$(libs_path)/gt $(thrift_links_path)
gt_links = $(main_links) -lip_netsdk
$(shell mkdir -p $(TARGET)/gt)
gt.exe :$(main_objs)
	$(COMPILE) src/sdks/gt.cpp $(main_objs) $(gt_include) $(gt_links_path) $(gt_links) -o $(TARGET)/gt/gt.exe

#**********xm**********
xm_include = -I $(libs_path)/xm $(main_include)
xm_links_path = -L$(libs_path)/xm $(thrift_links_path)
xm_links = $(main_links) -lxmnetsdk
$(shell mkdir -p $(TARGET)/xm)
xm.exe :$(main_objs)
	$(COMPILE) src/sdks/xm.cpp $(main_objs) $(xm_include) $(xm_links_path) $(xm_links) -o $(TARGET)/xm/xm.exe -Wno-conversion-null
