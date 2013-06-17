package org.jeo.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure representing the primary key of a table. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class PrimaryKey {

    List<PrimaryKeyColumn> columns = new ArrayList<PrimaryKeyColumn>();

    /**
     * The columns making up the primary key.
     */
    public List<PrimaryKeyColumn> getColumns() {
        return columns;
    }

    /**
     * Looks up a primary key column by name.
     * 
     * @param name The column name.
     * 
     * @return The column object or <code>null</code> if no such match.
     */
    public PrimaryKeyColumn column(String name) {
        for (PrimaryKeyColumn col : columns) {
            if (col.getName().equals(name)) {
                return col;
            }
        }
        return null;
    }
}
