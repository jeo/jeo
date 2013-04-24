# jeo - A Java Geo Library

## Building jeo

Before building jeo a few upstream libraries must first be built and installed in the 
local maven repository.

### Build proj4j

Build the ``jeo`` branch of the [proj4j repository](https://github.com/jdeolive/proj4j).

    git clone https://github.com/jdeolive/proj4j.git
    cd proj4j
    git checkout jeo
    mvn install

### Build GeoGIT

Build the ``master`` branch of the [geogit repoistory](https://github.com/opengeo/GeoGit/).

    git clone https://github.com/opengeo/GeoGit.git
    cd GeoGit
    cd src/parent
    mvn install

### Build jeo

    git clone https://github.com/jdeolive/jeo
    cd jeo
    mvn install
