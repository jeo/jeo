package org.jeo.feature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

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
}
