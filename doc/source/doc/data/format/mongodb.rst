.. _format_mongodb:

MongoDB
=======

Provides access to features in a `MongoDB`_ database.

.. _MongoDB: http://postgis.net

| MongoDB is an open-source, document-oriented database designed for ease of 
  development and scaling.

The MongoDB driver supports the alias "mongo". 

Usage
-----

The :jeoapi:`org.jeo.mongo.MongoDB` class is used to open a 
connection to a MongoDB database.

.. code-block:: java

   Workspace ws = MongoDB.open(new MongoOpts("jeo")
      .host("localhost").port(27017)
      .user("jdeolive").passwd("secret"));

Alternatively a map of connection keys can be used.

.. code-block:: java

   Map<Key,Object> opts = new HashMap<Key,Object>();
   opts.put(MongoDB.HOST, "localhost");
   opts.put(MongoDB.PORT, 5432);
   opts.put(MongoDB.USER, "jdeolive");
   opts.put(MongoDB.PASSWD, "secret");
   opts.put(MongoDB.DB, "jeo");

   MongoDB drv = new MongoDB();
   Workspace ws = drv.open(opts);

The *DB* parameter must always be specified. Other connection parameters are
optional with the following default values.

* *HOST* - localhost
* *PORT* - 27017
* *USER* - System.getProperty("user.name")
* *PASSWD* - blank

So the above connection could be created with:

.. code-block:: java

   Workspace ws = MongoDB.open(new MongoOpts("jeo"));

Assuming that the database does not require a password for localhost 
connections.

Versions
--------

The MongoDB driver works with versions 2.4+. While the driver may be able to 
read from earlier versions 2.4 is highly recommended. Version 2.4 of Mongo 
adds much more complete `geospatial support`_. 

.. _geospatial support: http://docs.mongodb.org/manual/applications/geospatial-indexes/

Geometry Objects
----------------

The MongoDB driver only recognizes geometry objects that are stored as GeoJSON.
As mentioned above version 2.4 of MongoDB added native support for GeoJSON 
geometry objects.

.. note:: MongoDB GeoJSON Support

   Currently only the primitive geometry types: ``Point``, ``LineString``, and 
   ``Polygon`` are supported by MongoDB. 

Feature Mapping
---------------

MongoDB, like other NoSQL databases, places no restrictions on the structure of 
documents stored in the database. In order for the MongoDB driver to map 
documents to feature objects some mapping information must be specified. A 
mapping specifies how to map document elements to feature properties. 

Two types of mappings are supported out of the box. The first involves 
specifying document "paths" to indicate what properties of a document refer to 
geometry objects. For example consider the following document.

.. code-block:: javascript

   {
     "geo": {
         "shape": {
            "type": "Polygon", 
            "coordinates": [...]
         }
     }, 
     "name": "foo"
   }

To isolate the "shape" property within the "geo" property the path "geo.shape"
would be used. Mappings may be specified at the data set level or the workspace 
level. Mappings are specified with the 
:jeoapi:`org.jeo.mongo.Mapping` class.

.. code-block:: java

   Mapping mapping = new Mapping().geometry("geo.shape");

   Workspace db = MongoDB.open(new MongoOpts("jeo"));
   db.setMapper(new DefaultMapper(mapping));

The above would map all data sets in the workspace in this way, which may not be
desirable.

.. code-block:: java

   MongoDataset data = db.get(...);
   data.setMapper(new DefaultMapper(mapping));


The second type of supported mapping assumes mongo documents are stored as 
valid GeoJSON features. As GeoJSON the above document looks like the following.

.. code-block:: javascript

   {
     "geometry": {
        "type": "Polygon", 
        "coordinates": [...]
     }, 
     "properties": {
         "name": "foo"
     }
   }

The :jeoapi:`org.jeo.mongo.GeoJSONMapper` class is used to 
declare this type of mapping.

.. code-block:: java

   MongoDataset data = db.get(...);
   data.setMapper(new GeoJSONMapper());

Maven
-----

.. parsed-literal::

  <dependency>
   <groupId>org.jeo</groupId>
   <artifactId>jeo-mongo</artifactId>
   <version>|version|</version>
  </dependency>

Dependencies
------------

The MongoDB driver depends on the `MongoDB Java driver`_. Currently version 
2.9.3 is used by default. 

.. _MongoDB Java driver: http://docs.mongodb.org/ecosystem/drivers/java/
