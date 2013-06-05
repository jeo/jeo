.. _csv:

CSV
===

Provides access to features in Comma Separated Value format. 

Usage
-----

The :jeoapi:`org.jeo.csv.CSV` class is used to open a csv file.

.. code-block:: java

   VectorData csv = CSV.open(new File("states.csv"), new CSVOpts());

The :jeoapi:`org.jeo.csv.CSVOpts` class is used to specify options 
controlling how the csv file is read. Supported options include:

* *DELIM* - The column delimiter, default ","
* *HEADER* - Whether the csv file has a header row, default true
* *X* - The x column
* *Y* - The y column

For example, consider the following csv file::

  CITY, LAT, LON
  Vancouver, 49.2505, -123.1119
  Calgary, 51.0544, -114.0669
  Toronto, 43.6481, -79.4042

.. code-block:: java

   CSV.open(new File("cities.csv"), new CSVOpts().xy("LON", "LAT"));

If the separator changed to whitespace which is common in csv files::

  CITY LAT LON
  Vancouver 49.2505 -123.1119
  Calgary 51.0544 -114.0669
  Toronto 43.6481 -79.4042

.. code-block:: java

   new CSVOpts().delimiter(Delimiter.whitespace()).xy("LON", "LAT");

Maven
-----

.. code-block:: xml

  <dependency>
   <groupId>org.jeo</groupId>
   <artifactId>jeo-csv</artifactId>
   <version>|version|</version>
  </dependency>

Dependencies
------------

The CSV driver has no third party dependencies.
