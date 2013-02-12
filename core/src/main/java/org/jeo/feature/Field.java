package org.jeo.feature;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class Field {

    String name;
    Class<?> type;
    CoordinateReferenceSystem crs;

    public Field(String name, Class<?> type) {
        this(name, type, null);
    }

    public Field(String name, Class<?> type, CoordinateReferenceSystem crs) {
        this.name = name;
        this.type = type != null ? type : Object.class;
        this.crs = crs;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isGeometry() {
        return Geometry.class.isAssignableFrom(type);
    }

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
