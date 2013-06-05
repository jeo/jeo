.. proj:

Projections
===========

Jeo utilizes the `PROJ4J`_ library for projection and coordinate reference 
system support.

.. _PROJ4J: http://trac.osgeo.org/proj4j

The :jeoapi:`org.jeo.proj.Proj` class provides convenience methods for 
creating coordinate reference system objects and preforming transformations 
between them.

.. code-block:: java

   // canonical geopgraphic
   CoordinateReferenceSystem crs1 = Proj.crs("epsg:4326");

   // google mercator
   CoordinateReferenceSystem crs2 = Proj.crs("+proj=merc", "+a=6378137",
     "+b=6378137", "+lat_ts=0.0", "+lon_0=0.0", "+x_0=0.0", "+y_0=0", "+k=1.0",
     "+units=m", "+nadgrids=@null", "+wktext", "+no_defs");

   // re-project
   Point p = Geom.point(-115, 51);
   p = Proj.reproject(p, crs1, crs2);
