package org.jeo.geojson;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.feature.Feature;
import org.jeo.util.Convert;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJSONReader {

    public Geometry geometry(Object json) {
        return (Geometry) parse(json, new GeometryHandler());
        
    }

    public Point point(Object json) {
        return (Point) geometry(json); 
    }

    public LineString lineString(Object json) {
        return (LineString) geometry(json);
    }

    public Polygon polygon(Object json) {
        return (Polygon) geometry(json);
    }

    public MultiPoint multiPoint(Object json) {
        return (MultiPoint) geometry(json);
    }

    public MultiLineString multiLineString(Object json) {
        return (MultiLineString) geometry(json);
    }

    public MultiPolygon multiPolygon(Object json) {
        return (MultiPolygon) geometry(json);
    }

    public GeometryCollection geometryCollection(Object json) {
        return (GeometryCollection) geometry(json);
    }

    public Feature feature(Object json) {
        return (Feature) parse(json, new FeatureHandler());
    }

    public Cursor<Feature> features(Object json) {
        try {
            return new GeoJSONCursor(toReader(json));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object read(Object json) {
        UnkownHandler h = new UnkownHandler();
        Object result = parse(json, h);

        if (h.getHandler() instanceof FeatureCollectionHandler) {
            return Cursors.create((Collection<Feature>)result);
        }

        return result;
    }

    Reader toReader(Object input) throws IOException {
        return Convert.toReader(input).get("unable to turn " + input + " into reader");
    }

    Object parse(Object input, BaseHandler handler) {
        try {
            return parse(toReader(input), handler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Object parse(Reader input, BaseHandler handler) throws IOException {
        JSONParser p = new JSONParser();
        RootHandler h = new RootHandler(handler);
        try {
            p.parse(input, h);
            return h.getValue();
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
