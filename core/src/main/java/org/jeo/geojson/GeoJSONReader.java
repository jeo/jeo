package org.jeo.geojson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.MapFeature;
import org.jeo.feature.Schema;
import org.jeo.geom.Geometries;
import org.jeo.proj.Proj;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJSONReader {

    GeometryFactory gf = new GeometryFactory();

    public Object read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();

        byte[] buf = new byte[1024];
        int n = -1;
        while((n = in.read(buf)) != -1) {
            sb.append(new String(buf, 0, n));
        }

        return read(sb.toString());
    }

    public Object read(String json) {
        try {
            return read(new JSONObject(json));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Object read(JSONObject obj) {
        try {
            
            if (!obj.has("type")) {
                throw new IllegalArgumentException("Object has no 'type' property");
            }

            String type = obj.getString("type");
            
            if ("Point".equalsIgnoreCase(type)) {
                return readPoint(obj);
            }
            else if ("LineString".equalsIgnoreCase(type)) {
                return readLineString(obj);
            }
            else if ("Polygon".equalsIgnoreCase(type)) {
                return readPolygon(obj);
            }
            else if ("MultiPoint".equalsIgnoreCase(type)) {
                return readMultiPoint(obj);
            }
            else if ("MultiLineString".equalsIgnoreCase(type)) {
                return readMultiLineString(obj);
            }
            else if ("MultiPolygon".equalsIgnoreCase(type)) {
                return readMultiPolygon(obj);
            }
            else if ("GeometryCollection".equalsIgnoreCase(type)) {
                return readGeometryCollection(obj);
            }
            else if ("Feature".equalsIgnoreCase(type)) {
                return readFeature(obj);
            }
            else if ("FeatureCollection".equalsIgnoreCase(type)) {
                return readFeatureCollection(obj);
            }
            else if ("Schema".equalsIgnoreCase(type)) {
                return readSchema(obj);
            }
            else {
                throw new IllegalArgumentException("Unrecognized object type: " + type);
            }
        }
        catch(JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public Point readPoint(JSONObject obj) throws JSONException {
        JSONArray arr = obj.getJSONArray("coordinates");
        return gf.createPoint(coord(arr));
    }

    public LineString readLineString(JSONObject obj) throws JSONException {
        JSONArray arr = obj.getJSONArray("coordinates");
        return gf.createLineString(coords(arr));
    }

    public Polygon readPolygon(JSONObject obj) throws JSONException {
        JSONArray arr = obj.getJSONArray("coordinates");
        return polygon(arr);
    }

    public MultiPoint readMultiPoint(JSONObject obj) throws JSONException {
        return gf.createMultiPoint(coords(obj.getJSONArray("coordinates")));
    }

    public MultiLineString readMultiLineString(JSONObject obj) throws JSONException {
        JSONArray arr = obj.getJSONArray("coordinates");
        LineString[] lines = new LineString[arr.length()];

        for (int i = 0; i < arr.length(); i++) {
            lines[i] = gf.createLineString(coords(arr.getJSONArray(i)));
        }
        return gf.createMultiLineString(lines);
    }

    public MultiPolygon readMultiPolygon(JSONObject obj) throws JSONException {
        JSONArray arr = obj.getJSONArray("coordinates");
        Polygon[] polys = new Polygon[arr.length()];

        for (int i = 0; i < arr.length(); i++) {
            polys[i] = polygon(arr.getJSONArray(i));
        }

        return gf.createMultiPolygon(polys);
    }

    public GeometryCollection readGeometryCollection(JSONObject obj) throws JSONException {
        JSONArray arr = obj.getJSONArray("geometries");
        Geometry[] geoms = new Geometry[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            geoms[i] = (Geometry) read(arr.getJSONObject(i));
        }
        return gf.createGeometryCollection(geoms);
    }

    public Feature readFeature(JSONObject obj) throws JSONException {

        Map<String,Object> map = new LinkedHashMap<String, Object>();
        if (obj.has("geometry")) {
            map.put("geometry", read(obj.getJSONObject("geometry")));
        }
        if (obj.has("properties")) {
            //TODO: this assumes all simple properties
            JSONObject props = obj.getJSONObject("properties");
            for (Iterator<?> it = props.keys(); it.hasNext();) {
                String key = it.next().toString();
                map.put(key, props.get(key));
            }
        }
        
        MapFeature f = new MapFeature(map);
        if (obj.has("crs")) {
            f.setCRS(readCRS(obj.getJSONObject("crs")));
        }
        return f;
    }

    public List<Feature> readFeatureCollection(JSONObject obj) throws JSONException {
        List<Feature> features = new ArrayList<Feature>();

        CoordinateReferenceSystem crs = null;
        if (obj.has("crs")) {
            crs = readCRS(obj.getJSONObject("crs"));
        }

        JSONArray arr = obj.getJSONArray("features");
        for (int i = 0; i < arr.length(); i++) {
            Feature f = readFeature(arr.getJSONObject(i));
            if (crs != null && f.getCRS() == null) {
                f.setCRS(crs);
            }

            features.add(f);
        }

        return features;
    }

    public Schema readSchema(JSONObject obj) throws JSONException {
       //not part of GeoJSON
        String name = obj.getString("name");
        if (name == null) {
            throw new IllegalArgumentException("Object must specify name property");
        }

        JSONObject properties = obj.getJSONObject("properties");
        List<Field> fields = new ArrayList<Field>();

        for (Iterator<?> it = properties.keys(); it.hasNext(); ) {
            String key = it.next().toString();
            JSONObject prop = properties.getJSONObject(key);

            String type = prop.getString("type");
            Class<?> clazz = null; 

            //first try as geometry
            if (Geometries.fromName(type) != null) {
                clazz = Geometries.fromName(type).getType();
            }
            else {
                //try as a primitive
                try {
                    clazz = Class.forName("java.lang." + 
                        Character.toUpperCase(type.charAt(0)) + type.substring(1));
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("type " +type+ " not supported"); 
                }
            }

            CoordinateReferenceSystem crs = null;
            if (prop.has("crs")) {
                crs = readCRS(prop.getJSONObject("crs"));
            }

            fields.add(new Field(key, clazz, crs));
        }

        return new Schema(name, fields);
    }

    public CoordinateReferenceSystem readCRS(JSONObject obj) throws JSONException {
        String type = obj.getString("type");
        if ("link".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Linked CRS objects not supported");
        }

        String name = obj.getJSONObject("properties").getString("name");
        if (name == null) {
            throw new IllegalArgumentException("No properties.name object");
        }

        return Proj.crs(name);
    }

    Coordinate coord(JSONArray arr) throws JSONException {
        Coordinate c = new Coordinate(arr.getDouble(0), arr.getDouble(1));
        if (arr.length() > 2) {
            c.z = arr.getDouble(2);
        }
        return c;
    }

    Coordinate[] coords(JSONArray arr) throws JSONException {
        Coordinate[] c = new Coordinate[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            c[i] = coord(arr.getJSONArray(i));
        }
        return c;
    }

    Polygon polygon(JSONArray arr) throws JSONException {
        LinearRing outer = gf.createLinearRing(coords(arr.getJSONArray(0)));
        if (arr.length() == 1) {
            return gf.createPolygon(outer);
        }

        LinearRing[] inner = new LinearRing[arr.length()-1];
        for (int i = 1; i < arr.length(); i++) {
            inner[i-1] = gf.createLinearRing(coords(arr.getJSONArray(i)));
        }

        return gf.createPolygon(outer, inner);
    }
}
