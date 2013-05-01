package org.jeo.feature;

import java.util.ArrayList;
import java.util.List;

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
    List<Field> fields = new ArrayList<Field>();

    /**
     * Creates a new builder object.
     * 
     * @param name The name of the schema.
     */
    public SchemaBuilder(String name) {
        this.name = name;
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
        fields.add(new Field(name, type));
        return this;
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
        fields.add(new Field(name, type, crs));
        return this;
    }

    /**
     * Returns the built schema.
     */
    public Schema schema() {
        return new Schema(name, fields);
    }
}
