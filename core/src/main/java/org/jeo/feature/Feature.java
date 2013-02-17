package org.jeo.feature;

import java.util.List;
import java.util.Map;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * An object consisting of a set of named attributes, any of which may be a vector geometry. 
 * 
 * @see ListFeature
 * @see MapFeature
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class Feature {

    /**
     * Feature schema.
     */
    protected Schema schema;

    /**
     * Coordinate reference system
     */
    protected CoordinateReferenceSystem crs;

    /**
     * Constructor for Feature not taking a schema object.
     */
    protected Feature() {
        this(null);
    }
   
    /**
     * Constructor for Feature taking an explicit schema.
     */
    protected Feature(Schema schema) {
        this.schema = schema;
    }

    /**
     * Coordinate reference system for the feature.
     * <p>
     * Generally the method {@link #crs()} should be used.
     * </p>
     * @return The crs, or <code>null</code> if none been set.
     */
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    /**
     * Sets the coordinate reference system for the feature.
     */
    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * The derived coordinate reference system for the feature.
     * <p>
     * If {@link #getCRS()} returns a value it is returned, otherwise if the feature has a 
     * schema object then {@link Schema#crs()} is returned. Otherwise this method returns 
     * <code>null</code>.
     * </p>
     * @return The derived crs.
     */
    public CoordinateReferenceSystem crs() {
        if (crs != null) {
            return crs;
        }

        if (schema != null) {
            return schema.crs();
        }

        return null;
    }

    /**
     * Gets a named attribute of the feature.
     * <p>
     * This method should return <code>null</code> if no such attribute named <tt>key</tt> exists.
     * </p>
     * @param key The key or name of the attribute.
     * 
     * @return The attribute value or <code>null</code>.
     */
    public abstract Object get(String key);

    /**
     * Sets a named attribute of the feature.
     *
     * @param key The key or name of the attribute. 
     * @param val The new value of the attribute. 
     */
    public abstract void put(String key, Object val);

    /**
     * Derived geometry of the feature.
     * <p>
     * If the feature object has a schema set then {@link Schema#geometry()} is used to locate 
     * a geometry object. If unavailable the {@link #findGeometry()} is used to locate a geometry
     * instance.
     * </p>
     * @return a {@link Geometry} object, or <code>null</code> if it could not be found.
     */
    public Geometry geometry() {
        if (schema != null) {
            Field f = schema.geometry();
            if (f != null) {
                return (Geometry) get(f.getName());
            }
        }

        return findGeometry();
    }

    /**
     * Method for subclasses to implement in order to find a geometry object when no schema
     * information is available.
     */
    protected abstract Geometry findGeometry();

    /**
     * The lazily created schema for the feature. 
     * <p>
     * If the {@link #schema} member is set for the feature it is returned. Otherwise the 
     * {@link #buildSchema()} is used to derive a schema for the feature.
     * </p>
     */
    public Schema schema() {
        if (schema == null) {
            schema = buildSchema();
        }
        return schema;
    }

    /**
     * Method for subclasses to implement to build a schema for the feature from its underlying
     * attributes.
     */
    protected abstract Schema buildSchema();

    /**
     * Returns an immutable list view of the feature
     */
    public abstract List<Object> list();

    /**
     * Returns an immutable map view of the feature.
     */
    public abstract Map<String,Object> map();
}
