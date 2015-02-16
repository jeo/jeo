# jeo - lightweight geo library for the jvm 

## Building

Building jeo requires the following:

* [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.7+
* [Apache Maven ](http://maven.apache.org/download.cgi) 3.1+

Once build pre-requisites are satisfied build with:

    mvn install

### Protocol Buffers

The library can be built with optional support for protocol buffers. This 
requires: [Google Protocol Buffers](https://developers.google.com/protocol-buffers/docs/downloads) 2.6+. Once installed:

    mvn -P protobuf install

See the [protobuf README](format/protobuf) for more details.