package org.jeo.geojson.parser;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;
import org.jeo.json.parser.ParseException;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class FeatureHandler extends BaseHandler {

    Integer id;

    public FeatureHandler() {
        this(null);
    }

    public FeatureHandler(Integer id) {
        this.id = id;
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException,
            IOException {
        if ("type".equals(key)) {
            push(key, new TypeHandler());
        }
        if ("crs".equals(key)) {
            push(key, new CRSHandler());
        }
        if ("geometry".equals(key)) {
            push(key, new GeometryHandler());
        }
        else if ("properties".equals(key)) {
            push(key, new PropertiesHandler());
        }
        else if ("id".equals(key)) {
            push(key, new IdHandler());
        }

        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        Geometry geom = node.consume("geometry", Geometry.class).or(null);

        Map<String,Object> props = node.consume("properties", Map.class)
            .or(new LinkedHashMap<String, Object>());

        props.put("geometry", geom);

        String fid = node.consume("id", String.class).or(id!=null?String.valueOf(id):null);

        Feature f = new BasicFeature(fid, props);
        f.setCRS(node.consume("crs", CoordinateReferenceSystem.class).or(null));

        node.setValue(f);

        pop();
        return true;
    }

    static class IdHandler extends BaseHandler {
        @Override
        public boolean primitive(Object value) throws ParseException, IOException {
            node.setValue(value);

            pop();
            return true;
        }
    }
}
