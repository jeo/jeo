.. _data_vector:

Vector Data
===========

In the previous section the :jeoapi:`org.jeo.data.VectorData` interface
was introduced. This interface is the primary abstraction used for access to a
vector data set.  

Features
--------

A vector dataset is a collection of :jeoapi:`org.jeo.feature.Feature` 
objects. A feature a simply a map of named attributes, any of which can be a 
geometry object.

.. code-block:: java

   // grab a feature 
   Feature f = ...;

   // get some attributes
   f.get("name");
   f.get("location")

   // set some attributes
   f.set("name", "foo")
   f.set("location", Geom.point(0,0));

Typically a feature object has a single geometry object. It is not uncommon for
a feature to contain multiple geometry objects, but in this case one is 
designated the default. The ``Feature.geometry()`` method is used to obtain 
the default geometry of a feature.

.. code-block:: java

   // grab a feature 
   Feature f = ...;

   // get the default geometry
   Geometry g = f.geometry();

   // set the default geometry
   f.put(Geom.point(0,0));

.. note:: Default Geometry

   Typically the default geometry of a feature is the first one encountered when
   iterating through the feature attributes. Also, it is perfectly valuid for a 
   feature to have no geometry attribute, in which case the 
   ``Feature.geometry()`` returns null.

A feature :jeoapi:`org.jeo.feature.Schema` is used to describe the 
structure and attributes of a feature object. A schema is a collection of 
:jeoapi:`org.jeo.feature.Field` objects, each field containing a name, a 
type, and an optional coordinate reference system.

.. code-block:: java

   // grab a feature
   Feature f = ...;

   // gets its schema
   Schema schema = f.schema();

   // iterate over all fields
   for (Field fld : schema) {
     System.out.println(fld.getName());
     System.out.println(fld.getType());
     Systme.out.println(fld.getCRS());
   }

.. todo:: Feature crs

.. todo:: Map and list view

Queries
-------

The :jeoapi:`org.jeo.data.Query` class is used to obtain features from a 
vector dataset. A query contains a number of properties that control what 
features are returned in a result set. This includes:

* bounding box - Spatial extent from which to return features
* attribute filter - Attribute predicate for which returned features must match
* limit - Maximum number of features to return
* offset - Offset into result set from which to start returning features

Additionally a query can specify options that transform returned features such
as:

* re-projection - Reproject geometries to a specific crs
* simplification - Simplify geometries with a specific tolerance

As an example:

.. code-block:: java

   // grab all features 
   Query q = new Query();

   // grab all features in a specific area
   Query q = new Query().bounds(new Envelope(...));

   // grab all features with some specific attributes
   Query q = new Query().filter("SAMP_POP > 2000000");

   // paged result set
   Query q = new Query().offset(100).limit(10);

   // reproject
   Query q = new Query().reproject("epsg:900913");

   // chain them all together
   Query q = new Query().bounds(new Envelope(...)).filter("SAMP_POP > 2000000")
     .offset(100).limit(10).reproject("epsg:900913");

.. todo:: sorting

Cursors
-------

The :jeoapi:`org.jeo.data.Cursor` class is used to return a result set 
of feature objects from a query. A cursor is for the most part an iterator in 
the normal java sense.

.. code-block:: java

   // get a dataset
   VectorData dataset = ...;

   // query it
   Cursor<Feature> c = dataset.cursor(new Query());

   // iterate
   whille (c.hasNext()) {
     Feautre f = c.next();
     System.out.println(f);
   }

   // close the cursor
   c.close();

A cursor implements ``java.util.Iterable`` and so the java for each provides
a shorthand for iterating through a cursor.

.. code-block:: java

   for (Feature f : dataset.cursor(new Query())) {
     System.out.println(f);
   }

.. note:: Closing Cursors

   It is important that a cursors ``Cursor.close`` method be called when it is
   no longer needed. When a cursor is used with a for-each as above the close 
   method will be called automatically upon loop completion. However if an
   exception or some other control flow event occurs causing the loop to 
   terminate prematurely it is up to the application to ensure close is 
   called. 

Cursors can also be used to write to a vector dataset. By default a cursor
is considered read-only. The ``Query.update`` and ``Query().append`` 
methods are used to obtain a write cursor. The former is used to update 
existing features of the dataset, and the latter is used to add new features.

The ``Cursor.write`` and ``Cursor.remove`` methods are used in write mode. 

.. code-block:: java

  // update every attribute value to a specific value
  Cursor<Feature> c = dataset.cursor(new Query().append());
  while (c.hasNext()) {
    Feature f = c.next();
    f.set("name", "foo");
    c.write();
  }
  
  // remove a feature
  Cursor<Feature> c = dataset.cursor(new Query().update());
  c.next();
  c.remove();
  
  // add a new feature to the dataset
  Cursor<Feature> c = dataset.cursor(new Query().append());
  Feature f = c.next();
  f.set("name", "bar");
  c.write();
  
