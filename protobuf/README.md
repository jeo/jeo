# jeo-protobuf

[Google Protocol Buffer](https://developers.google.com/protocol-buffers/) driver
for the [jeo](http://github.com/jeo/jeo) library.

# Building

Building the protobuf module requires the `protoc` utility. Sources and 
Windows binaries can be found 
[here](https://code.google.com/p/protobuf/downloads/list). Packages for OSX and
Linux are available through most package managers. Once installed ensure 
the `protoc` command is on the `PATH`. 

To build the protobuf module run maven:

    mvn install
