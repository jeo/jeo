package org.jeo.postgis;

public class PrimaryKeyColumn {

    String name;

    boolean autoIncrement;
    String sequence;

    public PrimaryKeyColumn(String name) {
        this.name = name;
        //this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
