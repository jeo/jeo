# jeo - lightweight geo library for the jvm 

[![](https://jitpack.io/v/jeo/jeo.svg)](https://jitpack.io/#jeo/jeo)

## Building

Building jeo requires the following:

* [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.7+
* [Apache Maven ](http://maven.apache.org/download.cgi) 3.1+

Once build pre-requisites are satisfied build with:

    mvn install

## Adding jeo to your application

Add the jitpack repository:
```
  allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
  }
```

Afterwards, you can add dependencies for the complete library including all submodules (`com.github.jeo:jeo:$version`) or or single submodules (`com.github.jeo.jeo:$submodule:$version`).