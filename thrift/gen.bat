#!/bin/sh
rm -rf gen-*

thrift -gen java netSDK.thrift
thrift -gen cpp  netSDK.thrift
