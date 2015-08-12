rd /S /Q gen-cpp gen-java

thrift -r -gen java netSDK.thrift
thrift -r -gen cpp  netSDK.thrift
