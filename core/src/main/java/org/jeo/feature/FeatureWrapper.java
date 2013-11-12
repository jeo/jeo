package org.jeo.feature;

import java.util.List;
import java.util.Map;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Wrapper class for {@link Feature}.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class FeatureWrapper implements Feature {

    protected Feature delegate;

    protected FeatureWrapper(Feature delegate) {
        this.delegate = delegate;
    }

    public Feature getDelegate() {
        return delegate;
    }

    public String getId() {
        return delegate.getId();
    }

    public CoordinateReferenceSystem getCRS() {
        return delegate.getCRS();
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        delegate.setCRS(crs);
    }

    public CoordinateReferenceSystem crs() {
        return delegate.crs();
    }

    public Object get(String key) {
        return delegate.get(key);
    }

    public Object get(int index) {
        return delegate.get(index);
    }

    public void put(String key, Object val) {
        delegate.put(key, val);
    }

    @Override
    public void put(Geometry g) {
        delegate.put(g);
    }

    public void set(int index, Object val) {
        delegate.set(index, val);
    }

    public Geometry geometry() {
        return delegate.geometry();
    }

    public Schema schema() {
        return delegate.schema();
    }

    public List<Object> list() {
        return delegate.list();
    }

    public Map<String, Object> map() {
        return delegate.map();
    }

    
}
