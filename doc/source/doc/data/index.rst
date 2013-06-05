.. data:

Working with Data
=================

.. toctree::
   :maxdepth: 1
   
   vector
   format/index

In jeo the :jeoapi:`org.jeo.data.Driver` interface is the abstraction 
for a spatial data format. A driver is responsible for reading data in a 
specific format. This list of supported drivers can be found in the 
:ref:`formats` section.

For instance to read GeoJSON data the 
:jeoapi:`org.jeo.geojson.GeoJSON` driver is used. The following
example reads a file named :file:`points.json`. 

.. code-block:: java

   GeoJSON drv = new GeoJSON();
   drv.open(new File("points.json"), null);

.. note:: Driver Options

   The second argument to the open method is a map of parameters that allow for
   specifying driver specific options when reading data.

The type of object returned from ``Driver.open`` depends on the driver. In the
above example an instance of the :jeoapi:`org.jeo.data.VectorData` 
interface is returned. This class is the abstraction for vector datasets and 
provides a number of data access methods.

.. code-block:: java

   // read the data
   VectorData data = new GeoJSON().open(new File("points.json"), null);

   // get the spatial bounds
   Envelope bbox = data.bounds();

   // count the number of features
   data.count(new Query());

   // iterate over the features
   for (Feature f : data.cursor(new Query())) {
      System.out.println(f);
   }

   // dispose
   data.close();

The above code snippet introduces two new classes. The 
:jeoapi:`org.jeo.data.Query` class is used to query a vector dataset
and controls what data is returned from the query. The 
:jeoapi:`org.jeo.feature.Feature` class represents an object in a 
vector dataset. Both of these concepts are covered in greater detail in the 
:ref:`next <data_vector>` section.

In the above GeoJSON example the driver was used to open a specific dataset. 
Other types of formats are containers for multiple datasets. An example is a 
PostGIS database.

.. code-block:: java

   // create a map of connection options
   Map opts = new HashMap();
   opts.put(PostGIS.HOST, "localhost");
   opts.put(PostGIS.PORT, 5432);
   opts.put(PostGIS.DB, "jeo");
   opts.put(PostGIS.USER, "jdeolive");

   // create a driver and open a connection
   PostGIS drv = new PostGIS();
   Workspace db = drv.open(opts);

In this example the result of ``Driver.open`` returns an instance of the 
:jeoapi:`org.jeo.data.Workspace` class. A workspace is a container for 
datasets. 

.. code-block:: java

   // iterate over layers in the workspace
   for (String dataset : db.list()) {
      System.out.println(dataset);
   }

   // get a specific dataset
   db.get("states");

   // dispose the workspace
   db.close();

.. note:: Disposing Resources

  In jeo all data objects such as workspaces and datasets extend from the 
  :jeoapi:`org.jeo.data.Disposable` interface and should be disposed 
  after use. This interface extends from ``java.io.Closeable`` and instance
  compatible with the Java 7 "try-with" block.  
