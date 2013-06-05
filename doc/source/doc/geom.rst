.. geom:

Geometries
==========

Jeo utilizes the `JTS`_ library for geometry support. 

.. _JTS: http://tsusiatsoftware.net/jts/main.html

The :jeoapi:`org.jeo.geom.Geom` class adds additional convenience 
methods for working with geometry objects. For example a builder object that 
makes it easy to build up complex geometry objects.

.. code-block:: java

   // build a polygon with a hole
   Geom.build().points(0,0,10,0,10,10,0,10,0,0).ring()
     .points(4,4,6,4,6,6,4,6,4,4).ring().toPolygon();


The ``Geom.iterate`` method makes it easy to iterate over geometry collections.

.. code-block:: java

   MultiPoint mp = Geom.build().points(0,0, 1,1, 2,2, 3,3).toMultiPoint();
   for (Point p : Geom.iterate(mp)) {
     // do something with p
   }
