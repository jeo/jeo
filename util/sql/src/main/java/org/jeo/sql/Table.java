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

import org.jeo.vector.Schema;

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
