package org.jeo.csv;

import java.util.Collections;
import java.util.List;

import org.jeo.feature.Field;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Strategy dealing with structure of CSV file. 
 */
public abstract class CSVHandler {

    public List<Field> header(String[] head) {
        return Collections.emptyList();
    }

    public abstract Geometry geom(String[] row);
}
