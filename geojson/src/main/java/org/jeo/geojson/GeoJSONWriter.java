package org.jeo.geojson;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.geom.Geom;
import org.json.simple.JSONArray;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class GeoJSONWriter {

    public static String toString(Geometry g) {
        StringWriter w = new StringWriter();
        try {
            new GeoJSONWriter(w).geometry(g);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return w.toString();
    }

    public static String toString(Feature f) {
        StringWriter w = new StringWriter();
        try {
            new GeoJSONWriter(w).feature(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return w.toString();
    }

    public static String toString(Cursor<Feature> features) throws IOException {
        StringWriter w = new StringWriter();
        new GeoJSONWriter(w).featureCollection(features);
        return w.toString();
    }

    Writer out;

    public GeoJSONWriter(Writer out) {
        this.out = out;
    }

    public Writer getWriter() {
        return out;
    }

    /**
     * Encodes a bounding box as a 4 element array.
     * 
     * @param b The The bounding box.
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#bounding-boxes}
     */
    public GeoJSONWriter bbox(Envelope b) throws IOException {

        JSONArray list = new JSONArray();
        list.add(b.getMinX());
        list.add(b.getMinY());
        list.add(b.getMaxX());
        list.add(b.getMaxY());
        list.writeJSONString(out);

        return this;
    }
    
    /**
     * Encodes a crs as a named crs.
     * 
     * @param crs The crs object.
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#named-crs}
     */
    public GeoJSONWriter crs(CoordinateReferenceSystem crs)  throws IOException {
        return write(toObject(crs));
    }

    public Map<String,Object> toObject(CoordinateReferenceSystem crs) {
        if (crs == null) {
            return null;
        }
        Map<String,Object> obj = object();
        
        obj.put("type", "name");

        Map<String,Object> props = object();
        props.put("name", crs.getName());

        obj.put("properties", props);
        return obj;
    }

    /**
     * Encodes a feature object.
     * 
     * @param f The feature.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-objects}
     */
    public GeoJSONWriter feature(Feature f)  throws IOException {
        return write(toObject(f));
    }

    public Map<String,Object> toObject(Feature f) {
        if (f == null) {
            return null;
        }

        Map<String,Object> obj = object();

        obj.put("type", "Feature");

        if (f.getId() != null) {
            obj.put("id", f.getId());
        }

        Geometry g = f.geometry();
        if (g != null) {
            obj.put("geometry", toObject(g));
        }

        obj.put("properties", f.map());
        return obj;
    }

    /**
     * Encoded a feature collection.
     *
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#feature-collection-objects}
     */
    public GeoJSONWriter featureCollection(Cursor<Feature> features)  throws IOException {
        Map<String,Object> obj = object();
        obj.put("type", "FeatureCollection");
        obj.put("features", new FeatureCollection(features));
        return write(obj);
    }

    //
    // geometry methods
    //
    /**
     * Encodes a geometry object.
     * 
     * @param g The geometry.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#geometry-objects}
     */
    public GeoJSONWriter geometry(Geometry g)  throws IOException {
        return write(toObject(g));
    }

    public Map<String,Object> toObject(Geometry g) {
        if (g == null) {
            return null;
        }

        switch(Geom.Type.from(g)) {
        case POINT:
            return toObject((Point)g);
        case LINESTRING:
            return toObject((LineString)g);
        case POLYGON:
            return toObject((Polygon)g);
        case MULTIPOINT:
            return toObject((MultiPoint)g);
        case MULTILINESTRING:
            return toObject((MultiLineString)g);
        case MULTIPOLYGON:
            return toObject((MultiPolygon)g);
        case GEOMETRYCOLLECTION:
            return toObject((GeometryCollection)g);
        }
        throw new IllegalArgumentException("Unable to encode " + g + " as GeoJSON");
    }
    
    /**
     * Encodes a point object.
     * 
     * @param p The point.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#point}
     */
    public GeoJSONWriter point(Point p) throws IOException {
        return write(toObject(p));
    }
    
    public Map<String,Object> toObject(Point p) {
        Map<String,Object> obj = object();
        obj.put("type", "Point");

        JSONArray l = new JSONArray();
        l.add(p.getX());
        l.add(p.getY());
        if (!Double.isNaN(p.getCoordinate().z)) {
            l.add(p.getCoordinate().z);
        }
        
        obj.put("coordinates", l);
        return obj;
    }
    
    /**
     * Encodes a linestring object.
     * 
     * @param l The linestring.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#linestring}
     */
    public GeoJSONWriter lineString(LineString l) throws IOException {
        return write(toObject(l));
    }
    
    public Map<String,Object> toObject(LineString l) {
        Map<String,Object> obj = object();
        obj.put("type", "LineString");
        obj.put("coordinates", toArray(l.getCoordinateSequence()));
        return obj;
    }
    
    /**
     * Encodes a polygon object.
     * 
     * @param p The polygon
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#polygon}
     */
    public GeoJSONWriter polygon(Polygon p) throws IOException {
        return write(toObject(p));
    }
    
    public Map<String,Object> toObject(Polygon p) {
        Map<String,Object> obj = object();
        obj.put("type", "Polygon");
        obj.put("coordinates", toArray(p)); 
        return obj;
    }
    
    /**
     * Encodes a multipoint object.
     * 
     * @param mp The multipoint
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multipoint}
     */
    public GeoJSONWriter multiPoint(MultiPoint mp) throws IOException {
        return write(toObject(mp));
    }
    
    public Map<String,Object> toObject(MultiPoint mp) {
        Map<String,Object> obj = object();
        obj.put("type", "MultiPoint");
        obj.put("coordinates", toArray(mp.getCoordinates()));
        return obj;
    }
    
    /**
     * Encodes a multilinestring object.
     * 
     * @param ml The multilinestring
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multilinestring}
     */
    public GeoJSONWriter multiLineString(MultiLineString ml) throws IOException {
        return write(toObject(ml));
    }
    
    public Map<String,Object> toObject(MultiLineString ml) {
        Map<String,Object> obj = object();
        obj.put("type", "MultiLineString");
        obj.put("coordinates", toArray(ml));
        return obj;
    }
    
    /**
     * Encodes a multipolygon object.
     * 
     * @param mp The multipolygon
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#multipolygon}
     */
    public GeoJSONWriter multiPolygon(MultiPolygon mp) throws IOException {
        return write(toObject(mp));
    }
    
    public  Map<String,Object> toObject(MultiPolygon mp) {
        Map<String,Object> obj = object();
        obj.put("type", "MultiPolygon");
        obj.put("coordinates", toArray(mp));
        return obj;
    }

    /**
     * Encodes a geometry collection object.
     * 
     * @param gc The geometry collection.
     * 
     * @see {@linkplain http://www.geojson.org/geojson-spec.html#geometry-collection}
     */
    public GeoJSONWriter geometryCollection(GeometryCollection gc) throws IOException {
        return write(toObject(gc));
    }
    
    public Map<String,Object> toObject(GeometryCollection gc) {
        Map<String,Object> obj = object();
        obj.put("type", "GeometryCollection");
        
        JSONArray l = new JSONArray();
        for (Geometry g : Geom.iterate(gc)) {
            l.add(toObject(g));
        }
        obj.put("geometries", l);
        return obj;
    }

    GeoJSONWriter write(Map<String,Object> obj) throws IOException {
        JSONValue.writeJSONString(obj, out);
        return this;
    }

    Map<String,Object> object() {
        return new LinkedHashMap<String, Object>();
    }

    JSONArray toArray(Coordinate[] coords) {
        return toArray(new CoordinateArraySequence(coords));
    }

    JSONArray toArray(CoordinateSequence coordseq) {
        JSONArray l = new JSONArray();
        int dim = coordseq.getDimension();
        for (int i = 0; i < coordseq.size(); i++) {
            JSONArray m = new JSONArray();
            m.add(coordseq.getOrdinate(i, 0));
            m.add(coordseq.getOrdinate(i, 1));
            if (dim > 2) {
                double v = coordseq.getOrdinate(i, 2);
                if (!Double.isNaN(v)) {
                    m.add(v);
                }
            }
            l.add(m);
        }
        return l;
    }

    JSONArray toArray(Polygon p) {
        JSONArray l = new JSONArray();
        l.add(toArray(p.getExteriorRing().getCoordinateSequence()));
        for (int i = 0; i < p.getNumInteriorRing(); i++) {
            l.add(toArray(p.getInteriorRingN(i).getCoordinateSequence()));
        }
        return l;
    }

    JSONArray toArray(MultiLineString ml) {
        JSONArray l = new JSONArray();
        for (LineString ls : Geom.iterate(ml)) {
            l.add(toArray(ls.getCoordinateSequence()));
        }
        return l;
    }

    JSONArray toArray(MultiPolygon mp) {
        JSONArray l = new JSONArray();
        for (Polygon p : Geom.iterate(mp)) {
            l.add(toArray(p));
        }
        return l;
    }

    class FeatureCollection implements JSONStreamAware {
        Cursor<Feature> cursor;

        FeatureCollection(Cursor<Feature> cursor) {
            this.cursor = cursor;
        }

        @Override
        public void writeJSONString(Writer out) throws IOException {
            out.write("[");

            for (Feature f : cursor) {
                out.write(JSONValue.toJSONString(toObject(f)));
            }

            out.write("]");
            out.flush();
        }

    }
}
