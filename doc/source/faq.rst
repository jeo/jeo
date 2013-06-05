.. faq:

Frequently Asked Questions
==========================

Why jeo?
--------

The jeo library comes from the desire to have a simple geo library in the Java 
world. Specifically a library that is carefully designed to be lightweight enough 
to use in environments other than the classic desktop and server. 

Does jeo work on Android?
-------------------------

Yes. The library was designed to be used in "sand boxed" environments where not
all functions of the standard JRE are available. 

Should I use jeo instead of other Java libraries like GeoTools?
---------------------------------------------------------------

It depends on your needs. The jeo philosophy is that the library should make it
simple to do simple things. GeoTools is an extremely powerful library that 
provides many different functions. Unfortunately this comes at the cost of 
bloat and api complexity. Jeo is not a replacement for GeoTools, it is an 
alternative for developers that have simpler requirements.

How is jeo licensed?
--------------------

Jeo is licensed under the `Apache Version 2.0 license`_. The core of library has 
the following runtime dependencies. 

* `JTS`_, licensed under the LGPL
* `PROJ4J`_, licensed under the Apache Version 2.0 license
* `SLF4J`_, licensed under the MIT license. 

.. _Apache Version 2.0 License: http://www.apache.org/licenses/LICENSE-2.0.html
.. _JTS: http://tsusiatsoftware.net/jts/main.html
.. _PROJ4J: http://trac.osgeo.org/proj4j
.. _SLF4J: http://www.slf4j.org
