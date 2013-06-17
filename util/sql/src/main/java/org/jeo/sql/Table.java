package org.jeo.sql;

import org.jeo.feature.Schema;

/**
 * Data structure represneting a database table.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class Table {

    String name;
    PrimaryKey primaryKey = new PrimaryKey();
    Schema schema;

    /**
     * Creates a new table object.
     * 
     * @param name The table name.
     */
    public Table(String name) {
        this.name = name;
    }

    /**
     * The table name.
     */
    public String getName() {
        return name;
    }

    /**
     * The table primary key.
     */
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Sets the table primary key.
     */
    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * The schema generated from the table.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Sets the schema generated from the table.
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
