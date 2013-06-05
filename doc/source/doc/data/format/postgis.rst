.. _format_postgis:

PostGIS
=======

Provides access to features in a `PostGIS`_ database.

.. _PostGIS: http://postgis.net

| PostGIS is a spatial database extender for PostgreSQL object-relational 
  database. It adds support for geographic objects allowing location queries to 
  be run in SQL.

The PostGIS driver supports the alias "pg".

Usage
-----

The :jeoapi:`org.jeo.postgis.PostGIS` class is used to open a 
connection to a PostGIS database.

.. code-block:: java

   Workspace ws = PostGIS.open(new PostGISOpts("jeo")
      .host("localhost").port(5432)
      .user("jdeolive").passwd("secret"));

Alternatively a map of connection keys can be used.

.. code-block:: java

   Map<Key,Object> opts = new HashMap<Key,Object>();
   opts.put(PostGIS.HOST, "localhost");
   opts.put(PostGIS.PORT, 5432);
   opts.put(PostGIS.USER, "jdeolive");
   opts.put(PostGIS.PASSWD, "secret");
   opts.put(PostGIS.DB, "jeo");

   PostGIS drv = new PostGIS();
   Workspace ws = drv.open(opts);

The *DB* parameter must always be specified. Other connection parameters are
optional with the following default values.

* *HOST* - localhost
* *PORT* - 5432
* *USER* - System.getProperty("user.name")
* *PASSWD* - blank

So the above connection could be created with:

.. code-block:: java

   Workspace ws = PostGIS.open(new PostGISOpts("jeo"));

Assuming that the database does not require a password for localhost 
connections.

Versions
--------

PostGIS versions 1.4+ are supported. 

Primary Key Mapping
-------------------

The PostGIS driver maps primary key columns to feature identifiers. The 
following type of primary keys are recognized.

* SERIAL (auto increment) columns
* Columns with an associated sequence, obtained through 
  ``pg_get_serial_sequence``

Maven
-----

.. parsed-literal::

  <dependency>
   <groupId>org.jeo</groupId>
   <artifactId>jeo-postgis</artifactId>
   <version>|version|</version>
  </dependency>

Dependencies
------------

The PostGIS driver depends on the `PostgreSQL JDBC driver`_. Currently
version 9.1-901.jdbc4 is shipped by default. 

.. _PostgreSQL JDBC driver: http://jdbc.postgresql.org
