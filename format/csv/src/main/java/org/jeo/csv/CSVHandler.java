package org.jeo.csv;

import java.io.IOException;
import java.util.List;

import com.csvreader.CsvReader;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Strategy dealing with structure of CSV file. 
 */
public abstract class CSVHandler {

    public abstract void header(CsvReader r) throws IOException;

    public abstract Geometry geom(CsvReader r) throws IOException;
}
