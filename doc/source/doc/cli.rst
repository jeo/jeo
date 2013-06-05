.. _cli:

Command Line Interface
======================

The :command:`jeo` command provides a command line tool for exploring data 
formats supported by jeo. The general syntax for the utility is:

.. code-block:: bash

   $ jeo
   usage: jeo <command> [<args>]

   Commands:

    drivers    Lists available format drivers
    query      Executes a query against a data source
    info       Provides information about a data source
    convert    Converts between data sources

   For detailed help on a specific command use jeo <command> -h

Data Sources
------------

Most commands take one or more data sources as input. A data source is a 
workspace, or a data set within a workspace. The following uri syntax is used
to reference a data source::

  [<driver>://][<primary-option>][?<secondary-options>]*][#<dataset>]

The following is an example a PostGIS uri::

  pg://jeo?host=localhost&port=5432&user=jdeolive#states

For file based data uris can be specified simply as a file path with the 
caveat that the file has an extension that identifies the driver. An example
of a GeoJSON path::

   /data/states.json

driver
^^^^^^

Name or alias identifying the driver to use to read the data source. Consult 
the :ref:`driver reference <formats>` for aliases for specific drivers. 

main-option
^^^^^^^^^^^ 

The main driver option used to connect to the data source. In the case of file
based drivers this is the file path. For database based drivers this is the name
of the database. 

secondary-options
^^^^^^^^^^^^^^^^^

Secondary driver options to use to connect to the data source. Secondary options
are specified as key value pairs. 

dataset
^^^^^^^

The name of a data set within a workspace. This option is only for workspace 
drivers. 

Commands
--------

drivers
^^^^^^^

The :command:`drivers` command lists all supported drivers.

  jeo drivers [options]

  Options:
    -x, --debug
       Runs command in debug mode
    -h, --help
       Provides help for this command
  
info
^^^^

The :command:`info` command provides information about a data source. 

  jeo info [options] datasource

  Options:
    -x, --debug
       Runs command in debug mode
    -h, --help
       Provides help for this command

When the data source specifies a workspace the command output will list all 
data sets of the workspace. When the data source specifies a data set 
information about the data set such as schema, spatial extent, and projection
will be displayed. 

query
^^^^^

The :command:`query` command provides information about a data set. 

  jeo query [options] dataset

  Options:
      -b, --bbox
         Bounding box (xmin,ymin,xmax,ymax)
      -f, --filter
         Predicate used to constrain results
      -c, -count
         Maximum number of results to return
      -s, -summary
         Summarize results only
      -x, --debug
         Runs command in debug mode
      -h, --help
         Provides help for this command


convert
^^^^^^^

The :command:`convert` command converts a data set between formats. 

  jeo convert [options] source target

  Options:
      -fc, --from-crs
         Source CRS override
      -tc, --to-crs
         Target CRS
      -h, --help
         Provides help for this command
      -x, --debug
         Runs command in debug mode

Examples
--------

List all supported drivers.

.. code-block:: bash

   $ jeo drivers

Info about data sets in a PostGIS workspace

.. code-block:: bash

   $ jeo info pg://jeo?host=localhost

Info about a specific data set in a PostGIS workspace

.. code-block:: bash

   $ jeo info pg://jeo#states

Query a GeoJSON data set

.. code-block:: bash

   $ jeo query states.json

Query a GeoJSON data set with spatial extent

.. code-block:: bash

   $ jeo query -b -124.731422,24.955967,-66.969849,49.371735 states.json

Query a GeoJSON data set with attribute filter

.. code-block:: bash

   $ jeo query -f "STATE_NAME = 'New York'" states.json

Convert a PostGIS table to GeoJSON

.. code-block:: bash

   $ jeo convert pg://jeo#states states.json

Reproject a GeoJSON file to PostGIS

.. code-block:: bash

   $ jeo convert -fc epsg:4326 -tc epsg:900913 states.json pg://jeo#states





