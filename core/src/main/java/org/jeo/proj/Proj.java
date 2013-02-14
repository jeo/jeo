package org.jeo.proj;

import org.jeo.geom.GeometryBuilder;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.proj.Projection;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class Proj {

    static CRSFactory csFactory = new CRSFactory();
    static CoordinateTransformFactory txFactory = new CoordinateTransformFactory();
    static GeometryBuilder gBuilder = new GeometryBuilder();

    public static CoordinateReferenceSystem crs(int srid) {
        return crs("EPSG:"+srid);
    }

    public static CoordinateReferenceSystem crs(String s) {
        return csFactory.createFromName(s);
    }

    public static Integer epsgCode(CoordinateReferenceSystem crs) {
        String name = crs.getName();
        if (name != null) {
            String[] split = name.split(":");
            if (split.length == 2 && "epsg".equalsIgnoreCase(split[0])) {
                return Integer.parseInt(split[1]);
            }
        }
        return null;
    }

    public static Envelope bounds(CoordinateReferenceSystem crs) {
        //TODO: this method is wildly inaccurate, find a better way to determine bounds 
        // from a projection/crs        
        Projection p = crs.getProjection();
        if (p != null) {
            CoordinateReferenceSystem geo = crs("epsg:4326");

            Point p1 = gBuilder.point(p.getMinLongitudeDegrees(), p.getMinLatitudeDegrees());
            Point p2 = gBuilder.point(p.getMaxLongitudeDegrees(), p.getMaxLatitudeDegrees());

            p1 = reproject(p1, geo, crs);
            p2 = reproject(p2, geo, crs);

            return new Envelope(p1.getX(), p2.getX(), p1.getY(), p2.getY());
        }

        return null;
    }

    public static <T extends Geometry> T reproject(T g, CoordinateReferenceSystem from, 
        CoordinateReferenceSystem to) {

        CoordinateTransform tx = txFactory.createTransform(from, to);
        if (tx == null) {
            throw new IllegalArgumentException("Unable to find transform from " + from + " to " + to);
        }

        g.apply(new CoordinateTransformer(tx));
        return g;
    }
}
