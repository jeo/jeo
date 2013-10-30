package org.jeo.csv;

import java.io.IOException;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Strategy dealing with structure of CSV file. 
 */
public abstract class CSVHandler {

    public abstract void header(List<String> head);

    public abstract Geometry geom(List<Object> row) throws IOException;
}
