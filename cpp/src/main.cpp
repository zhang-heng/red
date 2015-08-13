#include <stdlib.h>

#include <iostream>
#include <sstream>

#include "server.h"
#include <thread>

bool isNum(std::string str){
  std::stringstream sin(str);
  double d;
  char c;
  if(!(sin >> d))
    return false;
  if (sin >> c)
    return false;
  return true;
}

int main(int argc, char* argv[]){
  std::cout<< "sdk proc working on!" <<std::endl;
  if(argc == 2){
    std::string port_str(argv[1]);
    int port = 0;
    if(isNum(port_str)){
      port = atoi(port_str.c_str());
      if(0<port && port<65535){
        Server server(port);
        return 0;
      }
    }
  }
  std::cout<<"error"<<std::endl;
  return -1;
}
