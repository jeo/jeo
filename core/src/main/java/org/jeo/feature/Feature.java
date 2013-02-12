package org.jeo.feature;

import java.util.List;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public abstract class Feature {

    protected Schema schema;

    protected CoordinateReferenceSystem crs;

    protected Feature(Schema schema) {
        this.schema = schema;
    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public CoordinateReferenceSystem crs() {
        if (crs != null) {
            return crs;
        }

        if (schema != null) {
            return schema.crs();
        }

        return null;
    }

    public abstract Object get(String key);

    public abstract void put(String key, Object val);

    public Geometry geometry() {
        if (schema != null) {
            Field f = schema.geometry();
            if (f != null) {
                return (Geometry) get(f.getName());
            }
        }

        return findGeometry();
    }

    protected abstract Geometry findGeometry();

    public Schema schema() {
        if (schema == null) {
            schema = buildSchema();
        }
        return schema;
    }

    protected abstract Schema buildSchema();

    public abstract List<Object> values();
}
