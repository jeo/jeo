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
package org.jeo.feature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Collection;

/**
 * Builder for {@link Schema} objects.
 * <p>
 * Example usage:
 * <pre>
 * Schema schema = new SchemaBuilder("cities").field("loc", Point.class, "epsg:4326")
 *      .field("name", String.class).field("pop", Integer.class).schema();
 * </pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class SchemaBuilder {

    String name;
    String uri;
    List<Field> fields = new ArrayList<Field>();
    Map<String,Object> props;

    /**
     * Creates a new builder object.
     * 
     * @param name The name of the schema.
     */
    public SchemaBuilder(String name) {
        this.name = name;
    }

    /**
     * Sets the namespace of the schema.
     * 
     * @param uri A namespace uri.
     * 
     * @return This builder.
     */
    public SchemaBuilder uri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Adds a field to the schema being built.
     *  
     * @param name The field name.
     * @param type The field type/class;
     * 
     * @return This builder.
     */
    public SchemaBuilder field(String name, Class<?> type) {
        return field(name, (Class) type, (CoordinateReferenceSystem)null);
    }

    /**
     * Adds a geometry field to the schema being built.
     *  
     * @param name The field name.
     * @param type The field type/class;
     * @param crs The field srs/crs identifier.
     * 
     * @return This builder.
     */
    public SchemaBuilder field(String name, Class<? extends Geometry> type, String crs) {
        return field(name, type, Proj.crs(crs));
    }

    /**
     * Adds a geometry field to the schema being built.
     *  
     * @param name The field name.
     * @param type The field type/class;
     * @param crs The field crs.
     * 
     * @return This builder.
     */
    public SchemaBuilder field(String name, Class<? extends Geometry> type, CoordinateReferenceSystem crs) {
        return field(new Field(name, type, crs, props));
    }

    /**
     * Adds a field to the schema being built.
     *  
     * @param fld The field.
     * 
     * @return This builder.
     */
    public SchemaBuilder field(Field fld) {
        fields.add(fld);
        props = null;
        return this;
    }

    /**
     * Adds a collection/iterable of fields to the schema being built.
     * 
     * @param flds The fields to add.
     * 
     * @return This builder.
     */
    public SchemaBuilder fields(Iterable<Field> flds) {
        for (Field fld : flds) {
            fields.add(fld);
        }
        return this;
    }

    /**
     * Adds fields to the schema being built described by a GeoTools style schema specification
     * of the form: <pre>&lt;name>:&lt;type>[:srid=&lt;srid>][,...]</pre>
     * 
     * @param spec
     * @return
     */
    public SchemaBuilder fields(String spec) {
        for (String field : spec.split(" *, *")) {
            String[] split = field.split(" *: *");
            if (split.length < 1) {
                throw new IllegalArgumentException("field spec must have at least a name");
            }

            String name = split[0];
            Class<?> type;
            try {
                type = split.length > 1 ? classForName(split[1]) : Object.class;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("illegal type", e);
            }
            CoordinateReferenceSystem crs = null;
            if (split.length > 2) {
                String[] srid = split[2].split("=");
                Integer srs = Integer.parseInt(srid.length > 1 ? srid[1] : srid[0]);
                crs = Proj.crs(srs);
            }

            fields.add(new Field(name, type, crs));
        }
        return this;
    }

    Class<?> classForName(String name) throws ClassNotFoundException {
        if (name.contains(".")) {
            // already qualified
            return Class.forName(name);
        }

        // check for geometry type
        Geom.Type t = Geom.Type.from(name);
        if (t != null) {
            return t.getType();
        }

        // try in java.lang
        return Class.forName("java.lang." + name);
    }

    /**
     * Adds a property to be set on the next field.
     * <p>
     * This value is discarded after the next call to <tt>field()</tt>
     * </p>
     * @param key The property key.
     * @param value The property value.
     * 
     * @see {@link Field#getProperty(String)}
     */
    public SchemaBuilder property(String key, Object value) {
        if (props == null) {
            props = new LinkedHashMap<String, Object>();
        }
        props.put(key, value);
        return this;
    }

    /**
     * Returns the built schema.
     */
    public Schema schema() {
        return new Schema(name, uri, fields);
    }

    /**
     * Create a new Schema with only the specified fields. Fields not present
     * in the original with be ignored. The order of the fields will be retained
     * except those that are removed.
     * 
     * @param original The original Schema
     * @param retain The fields to retain
     * @return a new Schema with only the specified fields
     */
    public static Schema selectFields(Schema original, Collection<String> retain) {
        List<Field> fields = new ArrayList<Field>(retain.size());
        for (int i = 0; i < original.fields.size(); i++) {
            Field f = original.fields.get(i);
            if (retain.contains(f.getName())) {
                fields.add(f);
            }
        }
        return new Schema(original.name, original.uri, fields);
    }
}
