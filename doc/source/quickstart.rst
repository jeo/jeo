.. _quickstart:

QuickStart
==========

Prerequisites
-------------

Running jeo requires a Java Runtime Environment (JRE) 6 or greater. 

Download jeo
------------

Download the latest build and extract it somewhere on the system

.. code-block:: bash

    $ wget .../jeo-0.1.zip
    $ unzip jeo-0.1.zip

Update the PATH
---------------

Add the :file:`bin` directory from the unpacked archive to the path

.. code-block:: bash

    $ export PATH=$PATH:jeo-0.1/bin

Run the jeo Command
-------------------

At the command prompt execute the :command:`jeo` command

.. code-block:: bash

    $ jeo --version
    jeo 0.1 (8362df22e9aa9e03cb0dd8dadf695ea7a7e3c37d)

If everything is installed correctly version information should be printed to 
the console.

Further Reading
---------------

Consult the :ref:`cli` reference for more information about the jeo command 
line utility or continue on to :ref:`doc` to learn more about the library.
