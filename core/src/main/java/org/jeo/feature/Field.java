package org.jeo.feature;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Describes an attribute of a {@link Feature}.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Field {

    /** field name */
    String name;

    /** field type */
    Class<?> type;

    /** field crs */
    CoordinateReferenceSystem crs;

    public Field(String name, Class<?> type) {
        this(name, type, null);
    }

    public Field(String name, Class<?> type, CoordinateReferenceSystem crs) {
        this.name = name;
        this.type = type != null ? type : Object.class;
        this.crs = crs;
    }

    /**
     * The name of the field.
     */
    public String getName() {
        return name;
    }

    /**
     * The type of the field.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Determines if the field refers to a geometry type.
     * <p>
     * This method is short-hand for <code>Geometry.class.isAssignableFrom(geteType())</code>.
     * <p>
     * 
     * @return The crs object, or <code>null</code>.
     */
    public boolean isGeometry() {
        return Geometry.class.isAssignableFrom(type);
    }

    /**
     * Returns the coordinate reference system associated with the field, if set.
     * <p>
     * This value is typically only set when {@link #isGeometry()} returns <code>true</code>.
     * </p>
     */
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", name, getType().getSimpleName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((crs == null) ? 0 : crs.hashCode());
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
        Field other = (Field) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.equals(other.crs))
            return false;
        return true;
    }

    
}
