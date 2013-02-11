/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.jeo.geom;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * <p>
 * This code was taken from GeoTools. 
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public enum Geometries {
    POINT(Point.class),
    LINESTRING(LineString.class),
    POLYGON(Polygon.class),
    MULTIPOINT(MultiPoint.class),
    MULTILINESTRING(MultiLineString.class),
    MULTIPOLYGON(MultiPolygon.class),
    GEOMETRY(Geometry.class),
    GEOMETRYCOLLECTION(GeometryCollection.class);

    private final Class<? extends Geometry> type;
    private final String name;
    private final String simpleName;
    
    private Geometries(Class<? extends Geometry> type) {
        this.type = type;
        this.name = type.getSimpleName();
        this.simpleName = (name.startsWith("Multi") ? name.substring(5) : name);
    }
    
    /**
     * Return the {@code Geometry} class associated with this type.
     *
     * @return the {@code Geometry} class
     */
    public Class<? extends Geometry> getType() {
        return type;
    }

    /**
     * Equivalent to {@linkplain #getName()}.
     *
     * @return the name of this type
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Return a name for this type that is suitable for text descriptions.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the 'simple name'. Returns the same value as {@linkplain #getName()}
     * except for MULTIPOINT, MULTILINESTRING and MULTIPOLYGON, for which it returns
     * the name without the 'Multi' prefix.
     *
     * @return the simple name
     */
    public String getSimpleName() {
        return simpleName;
    }
    
    /**
     * Get the {@code Geometries} for the given object.
     *
     * @param geom a JTS Geometry object
     *
     * @return the {@code Geometries} for the argument's class, or {@code null}
     *         if the argument is {@code null}
     */
    public static Geometries fromObject(Geometry geom) {
        if (geom != null) {
            return fromClass(geom.getClass());
        }
    
        return null;
    }
    
    /**
     * Get the {@code Geometries} for the given {@code Geometry} class.
     *
     * @param geomClass the class
     *
     * @return the constant for this class
     */
    public static Geometries fromClass(Class<?> geomClass) {
        for (Geometries gt : Geometries.values()) {
            if (gt.type == geomClass) {
                return gt;
            }
        }
        
        //no direct match look for a subclass
        Geometries match = null;
    
        for (Geometries gt : Geometries.values()) {
            if (gt == GEOMETRY || gt == GEOMETRYCOLLECTION) {
                continue;
            }
            
            if (gt.type.isAssignableFrom(geomClass)) {
                if (match == null) {
                    match = gt;
                } else {
                    // more than one match
                    return null;
                }
            }
        }
        
        if (match == null) {
            //no matches from concrete classes, try abstract classes
            if (GeometryCollection.class.isAssignableFrom(geomClass)) {
                return GEOMETRYCOLLECTION;
            }
            if (Geometry.class.isAssignableFrom(geomClass)) {
                return GEOMETRY;
            }
        }
        
        return match;
    }
    
    /**
     * Get the {@code Geometries} for the specified name.
     * 
     * @param name The name of the geometry, eg: "POINT"
     * 
     * @return The constant for the name.
     */
    public static Geometries fromName(String name) {
        for (Geometries gt : Geometries.values()) {
            if (gt.getName().equalsIgnoreCase(name)) {
                return gt;
            }
        }
        return null;
    }
}