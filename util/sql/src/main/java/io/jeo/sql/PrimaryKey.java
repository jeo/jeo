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
