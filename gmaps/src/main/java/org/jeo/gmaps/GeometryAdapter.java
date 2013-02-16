package org.jeo.gmaps;

import java.util.ArrayList;
import java.util.List;

import org.jeo.geom.Geom;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryAdapter {

    public static class Handler {
        public void handle(MarkerOptions marker) {
        }

        public void handle(PolylineOptions polyline) {
        }

        public void handle(PolygonOptions polygon) {
        }
    }

    public void adapt(Geometry geom, Handler h) {
        Object obj = adapt(geom);
        handle(obj, h);
    }

    private void handle(Object obj, Handler h) {
        if (obj instanceof List) {
            for (Object o : (List<?>)obj) {
                handle(o, h);
            }
        }
        else {
            if (obj instanceof MarkerOptions) {
                h.handle((MarkerOptions)obj);
            }
            else if (obj instanceof PolylineOptions) {
                h.handle((PolylineOptions)obj);
            }
            else if (obj instanceof PolygonOptions) {
                h.handle((PolygonOptions)obj);
            }
        }
    }

    public Object adapt(Geometry geom) {
        switch(Geom.Type.fromObject(geom)) {
        case POINT:
            return adapt((Point)geom);
        case LINESTRING:
            return adapt((LineString)geom);
        case POLYGON:
            return adapt((Polygon)geom);
        case MULTIPOINT:
            return adapt((MultiPoint)geom);
        case MULTILINESTRING:
            return adapt((MultiLineString)geom);
        case MULTIPOLYGON:
            return adapt((MultiPolygon)geom);
        case GEOMETRYCOLLECTION:
            return adapt((GeometryCollection)geom);
        }

        return null;
    }
    
    public MarkerOptions adapt(Point p) {
        return new MarkerOptions().position(adapt(p.getCoordinate()));
    }
    
    public PolylineOptions adapt(LineString l) {
        PolylineOptions opts = new PolylineOptions();
        for (Coordinate c : l.getCoordinates()) {
            opts.add(adapt(c));
        }
        return opts;
    }
    
    public PolygonOptions adapt(Polygon p) {
        PolygonOptions opts = new PolygonOptions();
        for (Coordinate c : p.getExteriorRing().getCoordinates()) {
            opts.add(adapt(c));
        }
        return opts;
    }

    public List<MarkerOptions> adapt(MultiPoint mp) {
        List<MarkerOptions> list = new ArrayList<MarkerOptions>();
        for (Point p : Geom.iterate(mp)) {
            list.add(adapt(p));
        }
        return list;
    }

    public List<PolylineOptions> adapt(MultiLineString ml) {
        List<PolylineOptions> list = new ArrayList<PolylineOptions>();
        for (LineString l : Geom.iterate(ml)) {
            list.add(adapt(l));
        }
        return list;
    }

    public List<PolygonOptions> adapt(MultiPolygon mp) {
        List<PolygonOptions> list = new ArrayList<PolygonOptions>();
        for (Polygon p : Geom.iterate(mp)) {
            list.add(adapt(p));
        }
        return list;
    }

    public List<Object> adapt(GeometryCollection gc) {
        List<Object> list = new ArrayList<Object>();
        for (Geometry g : Geom.iterate(gc)) {
            list.add(adapt(g));
        }
        return list;
    }

    public LatLng adapt(Coordinate c) {
        return new LatLng(c.y, c.x);
    }

    public Coordinate adapt(LatLng ll) {
        return new Coordinate(ll.longitude, ll.latitude);
    }

    public LatLngBounds adapt(Envelope e) {
        return new LatLngBounds(adapt(new Coordinate(e.getMinX(), e.getMinY())), 
            adapt(new Coordinate(e.getMaxX(), e.getMaxY())));
    }

    public Envelope adapt(LatLngBounds llb) {
        Coordinate ll = adapt(llb.southwest);
        Coordinate ur = adapt(llb.northeast);
        return new Envelope(ll.x, ur.x, ll.y, ur.y);
    }

}
