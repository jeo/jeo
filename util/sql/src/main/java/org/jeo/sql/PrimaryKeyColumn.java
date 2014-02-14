/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.sql;

import org.jeo.feature.Field;

/**
 * Represents a column of a primary key.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class PrimaryKeyColumn {

    String name;
    Field field;
    boolean autoIncrement;
    String sequence;

    /**
     * Creates a new column.
     * 
     * @param name The column name.
     */
    public PrimaryKeyColumn(String name, Field field) {
        this.name = name;
        this.field = field;
    }

    /**
     * Returns the name of the column.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the schema field corresponding to the primary key column.
     */
    public Field getField() {
        return field;
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
