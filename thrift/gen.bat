rd /S /Q gen-cpp gen-java

..\\third-part\\windows\\thrift\\thrift.exe -r -gen java netSDK.thrift
..\\third-part\\windows\\thrift\\thrift.exe -r -gen cpp  netSDK.thrift
