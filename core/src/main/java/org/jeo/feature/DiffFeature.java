package org.jeo.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature wrapper that tracks changes to an underlying Feature object.
 * <p>
 * This class is used by 
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class DiffFeature implements Feature {

    Feature feature;
    Map<String,Object> changed;

    public DiffFeature(Feature feature) {
        this.feature = feature;
        changed = new HashMap<String, Object>();
    }

    /**
     * The underlying feature object.
     */
    public Feature getFeature() {
        return feature;
    }

    /**
     * The diff map.
     */
    public Map<String, Object> getChanged() {
        return changed;
    }

    public String getId() {
        return feature.getId();
    }

    public CoordinateReferenceSystem getCRS() {
        return feature.getCRS();
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        feature.setCRS(crs);
    }

    public CoordinateReferenceSystem crs() {
        return feature.crs();
    }

    public Object get(String key) {
        if (changed.containsKey(key)) {
            return changed.get(key);
        }

        return feature.get(key);
    }

    public void put(String key, Object val) {
        changed.put(key, val);
        feature.put(key, val);
    }

    public Geometry geometry() {
        return feature.geometry();
    }

    public Schema schema() {
        return feature.schema();
    }

    public List<Object> list() {
        return feature.list();
    }

    public Map<String, Object> map() {
        return feature.map();
    }

}
