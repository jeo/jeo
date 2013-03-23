package org.jeo.geotools;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;

public class GTFeature extends Feature {

    SimpleFeature feature;

    public GTFeature(SimpleFeature feature, Schema schema) {
        super(feature.getID(), schema);
        this.feature = feature;
    }

    @Override
    public Object get(String key) {
        return feature.getAttribute(key); 
    }

    @Override
    public void put(String key, Object val) {
        feature.setAttribute(key, val);
    }

    @Override
    protected Geometry findGeometry() {
        return (Geometry) feature.getDefaultGeometry();
    }

    @Override
    protected Schema buildSchema() {
        return null;
    }

    @Override
    public List<Object> list() {
        return feature.getAttributes();
    }

    @Override
    public Map<String, Object> map() {
        Map<String,Object> map = new LinkedHashMap<String, Object>();
        for (AttributeDescriptor ad : feature.getType().getAttributeDescriptors()) {
            String att = ad.getLocalName();
            map.put(att, get(att));
        }
        return map;
    }
}
