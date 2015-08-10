#!/bin/sh
rm -rf gen-*

thrift -r -gen java netSDK.thrift
thrift -r -gen cpp  netSDK.thrift
