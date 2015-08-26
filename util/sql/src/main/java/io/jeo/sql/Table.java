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
package io.jeo.sql;

import io.jeo.vector.Schema;

/**
 * Data structure represneting a database table.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class Table {

    String name;
    String schema;

    PrimaryKey primaryKey = new PrimaryKey();
    Schema type;

    /**
     * Creates a new table object.
     * 
     * @param name The table name.
     */
    public Table(String name, String schema) {
        this.name = name;
        this.schema = schema;
    }

    /**
     * The table name.
     */
    public String name() {
        return name;
    }

    /**
     * The optional schema.
     */
    public String schema() {
        return schema;
    }

    /**
     * The qualified name (schema + name) of the table.
     */
    public String qname() {
        return schema + "." + name;
    }

    /**
     * The table primary key.
     */
    public PrimaryKey primaryKey() {
        return primaryKey;
    }

    /**
     * Sets the table primary key.
     */
    public Table primaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    /**
     * The dataset schema generated from the table.
     */
    public Schema type() {
        return type;
    }

    /**
     * Sets the schema generated from the table.
     */
    public Table type(Schema type) {
        this.type = type;
        return this;
    }
}
