# jeo-geobuf

Mapbox [Geobuf](https://github.com/mapbox/geobuf/) driver for the jeo library.

# Re-generating Geobuf Sources

Generating sources from the `geobuf.proto` file reuqires requires the `protoc` 
utility. Sources and Windows binaries can be found 
[here](https://code.google.com/p/protobuf/downloads/list). Packages for OSX and
Linux are available through most package managers. Once installed ensure 
the `protoc` command is on the `PATH`. 

To build the geobuf module and re-generate the geobuf sources build with the 
`compile-pbf` profile.

    mvn -P compile-pbf install
