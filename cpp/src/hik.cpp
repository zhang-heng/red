#include<iostream>
#include "HCNetSDK.h"

std::string Version()
{
  NET_DVR_Init();
  return "hik";
}
