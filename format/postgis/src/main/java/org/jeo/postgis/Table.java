package org.jeo.postgis;

import org.jeo.feature.Schema;

public class Table {

    String name;
    PrimaryKey primaryKey = new PrimaryKey();
    Schema schema;

    public Table(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
