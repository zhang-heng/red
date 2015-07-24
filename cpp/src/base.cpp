#include<iostream>

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TSocket.h>
#include <thrift/transport/TTransportUtils.h>

std::string Version();

int main()
{
  std::cout << Version() << std::endl;
  return 0;
}
