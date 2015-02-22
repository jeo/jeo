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
package org.jeo.vector;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.osgeo.proj4j.CoordinateReferenceSystem;

/**
 * Describes the structure of a {@link Feature} object.
 * <p>
 * A schema is am immutable collection of named {@link Field} objects, any of which may be a 
 * geometry field. 
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class Schema implements Iterable<Field> {

    /** schema name */
    String name;

    /** schema namespace */
    String uri;

    /** list of fields */
    List<Field> fields;

    /**
     * Returns a new schema builder.
     * 
     * @param name The name of the schema.
     */
    public static SchemaBuilder build(String name) {
        return new SchemaBuilder(name);
    }

    /**
     * Constructs a new Schema.
     * 
     * @param name Name of the schema.
     * @param fields List of fields 
     */
    public Schema(String name, List<Field> fields) {
        this(name, null, fields);
    }

    /**
     * Constructs a new Schema.
     * 
     * @param name Name of the schema.
     * @param uri Namespace of the schema.
     * @param fields List of fields 
     */
    public Schema(String name, String uri, List<Field> fields) {
        this.name = name;
        this.uri = uri;
        this.fields = Collections.unmodifiableList(fields);
    }

    /**
     * Name of the schema.
     */
    public String getName() {
        return name;
    }

    /**
     * Namespace uri of the schema.
     * <p>
     * May be <code>null</code>
     * </p>
     */
    public String getURI() {
        return uri;
    }

    /**
     * Derived geometry field of the schema.
     * <p>
     * This method returns the first field that returns true from {@link Field#isGeometry()}. If 
     * no such field is found <code>null</code> is returned.
     * </p>
     */
    public Field geometry() {
        //TODO: store the derived result
        for (Field f : this) {
            if (f.isGeometry()) {
                return f;
            }
        }
        return null;
    }

    /**
     * Derived coordinate reference system for the schema.
     * <p>
     * This method delegates to <code>geometry().getCRS()</code>.
     * </p> 
     * @return The coordinate reference system object, or <code>null</code> if not available or 
     * no geometry field exists.
     */
    public CoordinateReferenceSystem crs() {
        Field g = geometry();
        return g != null ? g.crs() : null;
    }

    /**
     * The immutable list of fields for the schema.
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Returns the field of the schema with the specified name, or <code>null</code> if no such 
     * field exists.
     * 
     * @param name The field name.
     */
    public Field field(String name) {
        int i = indexOf(name);
        return i != -1 ? fields.get(i) : null;
    }

    /**
     * Returns the index of the field in the schema with the specified name, or <code>-1</code> if
     * no such field exists.
     * 
     * @param name The field name.
     * 
     * @return The index position, or <code>-1</code>
     */
    public int indexOf(String name) {
        //TODO: potentially add an index of name to field
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            if (f.name().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Number of fields in the schema.
     */
    public int size() {
        return fields.size();
    }

    /**
     * Iterator over the fields of the schema.
     */
    @Override
    public Iterator<Field> iterator() {
        return fields.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name).append("[");
        if (!fields.isEmpty()) {
            for (Field f : fields) {
                sb.append(f).append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Schema other = (Schema) obj;
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (uri == null) {
            if (other.uri != null)
                return false;
        } else if (!uri.equals(other.uri))
            return false;
        return true;
    }
}
