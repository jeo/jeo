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
public interface Feature {

    /**
     * Feature identifier.
     */
    String getId();

    /**
     * Coordinate reference system for the feature.
     * <p>
     * Generally the method {@link #crs()} should be used.
     * </p>
     * @return The crs, or <code>null</code> if none been set.
     */
    CoordinateReferenceSystem getCRS();

    /**
     * Sets the coordinate reference system for the feature.
     */
    void setCRS(CoordinateReferenceSystem crs);

    /**
     * The derived coordinate reference system for the feature.
     * <p>
     * If {@link #getCRS()} returns a value it is returned, otherwise if the feature has a 
     * schema object then {@link Schema#crs()} is returned. Otherwise this method returns 
     * <code>null</code>.
     * </p>
     * @return The derived crs.
     */
    CoordinateReferenceSystem crs();

    /**
     * Gets a named attribute of the feature.
     * <p>
     * This method should return <code>null</code> if no such attribute named <tt>key</tt> exists.
     * </p>
     * @param key The key or name of the attribute.
     * 
     * @return The attribute value or <code>null</code>.
     */
    Object get(String key);

    /**
     * Sets a named attribute of the feature.
     *
     * @param key The key or name of the attribute. 
     * @param val The new value of the attribute. 
     */
    void put(String key, Object val);

    /**
     * Derived geometry of the feature.
     * <p>
     * If the feature object has a schema set then {@link Schema#geometry()} is used to locate 
     * a geometry object. If unavailable the {@link #findGeometry()} is used to locate a geometry
     * instance.
     * </p>
     * @return a {@link Geometry} object, or <code>null</code> if it could not be found.
     */
    Geometry geometry();

    /**
     * The lazily created schema for the feature. 
     * <p>
     * If the {@link #schema} member is set for the feature it is returned. Otherwise the 
     * {@link #buildSchema()} is used to derive a schema for the feature.
     * </p>
     */
    Schema schema();

    /**
     * Returns an immutable list view of the feature
     */
    abstract List<Object> list();

    /**
     * Returns an immutable map view of the feature.
     */
    abstract Map<String,Object> map();
}
