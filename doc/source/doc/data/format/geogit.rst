.. _format_geogit:

GeoGIT
======

Provides access to features in a `GeoGIT`_ repository.

.. _GeoGIT: http://geogit.org

| GeoGit is a Distributed Version Control System (DVCS) specially designed to 
  handle geospatial data efficiently.

Usage
-----

The :jeoapi:`org.jeo.geogit.GeoGit` class is used to access a
GeoGIT repository.

.. code-block:: java

   Workspace ws = GeoGit.open(new GeoGitOpts(new File("repo")));

If the repository does not exist it can be created on demand.

.. code-block:: java

   Workspace ws = GeoGit.open(new GeoGitOpts(new File("repo")).create(true));

As with regular Git username and email may be specified. 

.. code-block:: java

   Workspace ws = GeoGit.open(new GeoGitOpts(new File("repo")).create(true)
    .user("jdeolive").email("jdeolive@jeo.org"));

Alternatively a map of connection keys can be used.

.. code-block:: java

   Map<Key,Object> opts = new HashMap<Key,Object>();
   opts.put(GeoGit.FILE, new File("repo"));
   opts.put(GeoGit.CREATE, true);
   opts.put(GeoGit.USER, "jdeolive");
   opts.put(GeoGit.EMAIL, "jdeolive@jeo.org");
   
   GeoGit drv = new GeoGit();
   Workspace ws = drv.open(opts);

The *FILE* parameter must always be specified. Other parameters are optional
with the following default values.

* *CREATE* - false
* *USER* - System.getProperty("user.name")
* *EMAIL* - *USER* + "@localhost"

Maven
-----

.. code-block:: xml

  <dependency>
   <groupId>org.jeo</groupId>
   <artifactId>jeo-geogit</artifactId>
   <version>|version|</version>
  </dependency>

Dependencies
------------

The GeoGIT driver depends on the GeoGIT library which has a number of 
dependencies. See the GeoGIT documentation for details.
