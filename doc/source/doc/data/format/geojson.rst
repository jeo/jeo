.. _format_geojson:

GeoJSON
=======

Provides access to features in `GeoJSON`_ format. 

.. _GeoJSON: http://www.geojson.org/geojson-spec.html

| GeoJSON is a format for encoding a variety of geographic data structures. A 
  GeoJSON object may represent a geometry, a feature, or a collection of 
  features. 

The GeoJSON driver supports the alias "json".

Usage
-----

The :jeoapi:`org.jeo.geojson.GeoJSON` driver class is used to open
a file containing encoded GeoJSON.

.. code-block:: java

   VectorData data = GeoJSON.open(new File("states.json"));

Additionally the driver can create a new dataset from scratch:

.. code-block:: java

   VectorData data = GeoJSON.open(new File("states.json"));

The :jeoapi:`org.jeo.geojson.GeoJSONReader` class provides a parser
for parsing GeoJSON from an input source. 

.. code-block:: java

   String json = "{ \"type\": \"Point\", \"coordinates\": [1.0, 2.0] }";

   GeoJSONReader parser = new GeoJSONReader();

   // call method knowing what to expect
   Point p = parser.point(json);

   // or call method not knowing what to expect
   Object obj = parser.read(json);

Similarily the :jeoapi:`org.jeo.geojson.GeoJSONWriter` class provides
an encoder. 

.. code-block:: java

   Point p = Geom.point(1.0,2.0);

   // encode as a string
   String json = GeoJSONWriter.toString(p);

   // encode to another output
   Writer w = new OutputStreamWriter(System.out);

   GeoJSONWriter encoder = new GeoJSONEncoder(w);
   encoder.point(p);

The writer class can be used to build up complex JSON objects as well.

.. code-block:: java

   GeoJSONWriter e = new GeoJSONEncoder(new OutputStreamWriter(System.out));

   e.obj();
   e.key("location").point(Geom.point(1,2));
   e.key("name").value("foo");
   e.endObj();

Issues
------

The GeoJSON driver can not update an existing GeoJSON data set therefore 
update cursors are not supported. Append cursor is supported only if the 
underlying GeoJSON file is empty. 

Maven
-----

.. parsed-literal::

  <dependency>
   <groupId>org.jeo</groupId>
   <artifactId>jeo-geojson</artifactId>
   <version>|version|</version>
  </dependency>

Dependencies
------------

The GeoJSON driver utilizes the `json-simple`_ library for JSON encoding and
decoding. 

.. _json-simple: http://code.google.com/p/json-simple/
