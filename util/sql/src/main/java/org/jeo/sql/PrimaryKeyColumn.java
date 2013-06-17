package org.jeo.sql;

/**
 * Represents a column of a primary key.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class PrimaryKeyColumn {

    String name;
    boolean autoIncrement;
    String sequence;

    /**
     * Creates a new column.
     * 
     * @param name The column name.
     */
    public PrimaryKeyColumn(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the column.
     */
    public String getName() {
        return name;
    }

    /**
     * Flag indicating if the column is auto incrementing.
     * 
     * @return <code>true</code> if the column is auto-incrementing.
     */
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    /**
     * Sets flag indicating if the column is auto-incrementing.
     */
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    /**
     * Returns the sequence attached to the column.
     * 
     * @return A sequence name, or <code>null</code> if there is no sequence attached to the column.
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Sets the sequence associated with the column.
     * 
     * @param sequence Sequence name.
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
