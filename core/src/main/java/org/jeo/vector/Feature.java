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

import java.util.List;
import java.util.Map;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * An object consisting of a set of named attributes, any of which may be a vector geometry. 
 * 
 * @see BasicFeature
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Feature {

    /**
     * Feature identifier.
     */
    String id();

    /**
     * The coordinate reference system for the feature.
     * <p>
     *  If necessary implementations should dervice from {@link Schema#crs()}.
     * </p>
     * @return The crs, or <code>null</code> if unknown.
     */
    CoordinateReferenceSystem crs();

    /**
     * Check if a feature has an attribute. This should return true if the key
     * exists even if the value is null.
     *
     * @return true if the attribute key is present, false otherwise.
     */
    boolean has(String key);

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
     * Gets an attribute of the feature by index.
     * 
     * @param index The attribute index.
     * 
     * @return The attribute value.
     */
    Object get(int index);

    /**
     * Geometry of the feature.
     *
     * @return a {@link Geometry} object, or <code>null</code> if the feature has no geometry.
     */
    Geometry geometry();

    /**
     * Sets a named attribute of the feature.
     *
     * @param key The key or name of the attribute. 
     * @param val The new value of the attribute.
     *
     * @return This object.
     */
    Feature put(String key, Object val);

    /**
     * Sets a feature attribute by index.
     * 
     * @param index The attribute index.
     * @param val The new value.
     *
     * @return This object.
     */
    Feature set(int index, Object val);

    /**
     * Sets the geometry of the feature.
     *
     * @return This object.
     */
    Feature put(Geometry g);

    /**
     * Get the schema of the feature, deriving it if necessary.
     * <p>
     * Equivalent to <tt>schema(true)</tt>
     * </p>
     */
    Schema schema();

    /**
     * Get a Schema for this Feature.
     * <p>
     *  The <tt>derive</tt> flag controls whether a schema for the feature should be
     *  derived if the feature does not have a native schema.
     * </p>
     */
    Schema schema(boolean derive);

    /**
     * Returns an immutable list view of the feature
     */
    abstract List<Object> list();

    /**
     * Returns an immutable map view of the feature.
     */
    abstract Map<String,Object> map();
}
