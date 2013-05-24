/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.jeo.shp.file;

import org.jeo.shp.Shapefile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 * @source $URL$
 */
public class StreamLogging {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shapefile.class);

    private String name;
    private int open = 0;

    /**
     * The name that will appear in the debug message
     * 
     * @param name
     */
    public StreamLogging(String name) {
        this.name = name;
    }

    /**
     * Call when reader or writer is opened
     */
    public synchronized void open() {
        open++;
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(name + " has been opened. Number open: " + open);
        }
    }

    public synchronized void close() {
        open--;
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(name + " has been closed. Number open: " + open);
        }
    }

}
