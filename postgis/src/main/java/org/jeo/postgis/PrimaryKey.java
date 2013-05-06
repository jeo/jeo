package org.jeo.postgis;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKey {

    List<PrimaryKeyColumn> columns = new ArrayList<PrimaryKeyColumn>();

    public List<PrimaryKeyColumn> getColumns() {
        return columns;
    }

    public PrimaryKeyColumn column(String name) {
        for (PrimaryKeyColumn col : columns) {
            if (col.getName().equals(name)) {
                return col;
            }
        }
        return null;
    }
}
