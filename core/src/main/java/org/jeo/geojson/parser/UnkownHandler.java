package org.jeo.geojson.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jeo.geojson.simple.parser.ParseException;

public class UnkownHandler extends BaseHandler {

    static Map<String,Class> HANDLERS = new HashMap<String,Class>();
    static {
        HANDLERS.put("Point", GeometryHandler.class);
        HANDLERS.put("LineString", GeometryHandler.class);
        HANDLERS.put("Polygon", GeometryHandler.class);
        HANDLERS.put("MultiPoint", GeometryHandler.class);
        HANDLERS.put("MultiLineString", GeometryHandler.class);
        HANDLERS.put("MultiPolygon", GeometryHandler.class);
        HANDLERS.put("GeometryCollection", GeometryHandler.class);
        HANDLERS.put("Feature", FeatureHandler.class);
        HANDLERS.put("FeatureCollection", FeatureCollectionHandler.class);
    }

    BaseHandler handler;

    public BaseHandler getHandler() {
        return handler;
    }

    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if (!"type".equals(key)) {
            throw new IllegalStateException("expected type property");
        }
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        String type = (String) value;
        if (!HANDLERS.containsKey(type)) {
            throw new IllegalArgumentException("unknown object type: " + type);
        }

        try {
            handler = (BaseHandler) HANDLERS.get(type).newInstance();
            if (handler instanceof FeatureCollectionHandler) {
                // special case for feature collections, when parsing unknown collection we
                // can't stream
                ((FeatureCollectionHandler) handler).setStreaming(false);
            }
            push("object", handler);
        } 
        catch (Exception e) {
            throw new IOException(e);
        }
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }
}
