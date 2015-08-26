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
package io.jeo.vector;

import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * An object consisting of a set of named attributes, any of which may be a vector geometry. 
 *
 * @see MapFeature
 * @see ListFeature
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Feature {

    /**
     * Feature identifier.
     */
    String id();

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
     * Geometry of the feature.
     *
     * @return a {@link Geometry} object, or <code>null</code> if the feature has no geometry.
     */
    Geometry geometry();

    /**
     * Sets a named attribute of the feature.
     *
     * @param key The key or name   of the attribute.
     * @param val The new value of the attribute.
     *
     * @return This object.
     */
    Feature put(String key, Object val);

    /**
     * Sets the geometry of the feature.
     *
     * @return This object.
     */
    Feature put(Geometry g);

    /**
     * Returns an immutable map view of the feature.
     */
    Map<String,Object> map();
}
