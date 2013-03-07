package org.jeo.proj;

import org.jeo.geom.GeometryBuilder;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.Proj4jException;
import org.osgeo.proj4j.ProjCoordinate;
import org.osgeo.proj4j.proj.Projection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * Projection module utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Proj {

    /** 
     * The canonical geographic coordinate reference system.
     */
    public static final CoordinateReferenceSystem EPSG_4326;

    static CRSFactory csFactory = new CRSFactory();
    static CoordinateTransformFactory txFactory = new CoordinateTransformFactory();
    static GeometryBuilder gBuilder = new GeometryBuilder();

    static {
        EPSG_4326 = Proj.crs("EPSG:4326");
    }

    /**
     * Looks up a crs object base on its EPSG identifier.  
     * <p>
     * This method is equivalent to calling <pre>crs("EPSG:" + srid)</pre> 
     * </p>
     * @return The matching crs object, or <code>null</code> if none found.
     */
    public static CoordinateReferenceSystem crs(int srid) {
        return crs("EPSG:"+srid);
    }

    /**
     * Looks up a crs object base on its identifier.  
     *
     * @return The matching crs object, or <code>null</code> if none found.
     */
    public static CoordinateReferenceSystem crs(String s) {
        return csFactory.createFromName(s);
    }

    /**
     * Returns the EPSG identifier for a crs object.  
     *
     * @return The epsg identifier, or null if the CRS has no epsg code.
     */
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

    /**
     * Returns the valid bounds of the crs object.
     * <p>
     * <i>Warning</i>: This method is as currently implemented is inaccurate.  
     * </p>
     * @return An approximate bounds of validity for the crs object, or <code>null</code> if it 
     *  can not be determined.
     */
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

    /**
     * Reprojects a geometry object between two coordinate reference systems.
     * <p>
     * In the event a transformation between the two crs objects can not be found this method throws
     * {@link IllegalArgumentException}.
     * 
     * In the event the two specified coordinate reference systems are equal this method is a 
     * no-op and returns the original geometry object. 
     * </p>
     * @param g The geometry to reproject.
     * @param from The source coordinate reference system.
     * @param to The target coordinate reference system.
     * 
     * @return The reprojected geometry.
     * 
     * @throws IllegalArgumentException If no coordinate transform can be found.
     */
    public static <T extends Geometry> T reproject(T g, CoordinateReferenceSystem from, 
        CoordinateReferenceSystem to) {

        CoordinateTransform tx = findTransform(from, to);
        if (tx instanceof IdentityCoordinateTransform) {
            return g;
        }

        g.apply((CoordinateSequenceFilter) new CoordinateTransformer(tx));
        return g;
    }

    /**
     * Reprojects an envelope between two coordinate reference systems.
     * <p>
     * In the event a transformation between the two crs objects can not be found this method throws
     * {@link IllegalArgumentException}.
     * 
     * In the event the two specified coordinate reference systems are equal this method is a 
     * no-op and returns the original envelope. 
     * </p>
     * @param e The envelope to reproject.
     * @param from The source coordinate reference system.
     * @param to The target coordinate reference system.
     * 
     * @return The reprojected envelope.
     * 
     * @throws IllegalArgumentException If no coordinate transform can be found.
     */
    public static Envelope reproject(Envelope e, CoordinateReferenceSystem from, 
        CoordinateReferenceSystem to) {
        
        CoordinateTransform tx = findTransform(from, to);
        if (tx instanceof IdentityCoordinateTransform) {
            return e;
        }

        CoordinateTransformer txr = new CoordinateTransformer(tx);

        Coordinate c1 = new Coordinate(e.getMinX(), e.getMinY());
        Coordinate c2 = new Coordinate(e.getMaxX(), e.getMaxY());
        txr.filter(c1);
        txr.filter(c2);

        return new Envelope(c1.x, c2.x, c1.y, c2.y);
    }

    private static CoordinateTransform findTransform(CoordinateReferenceSystem from, 
        CoordinateReferenceSystem to) {

        if (from.equals(to)) {
            return new IdentityCoordinateTransform();
        }

        CoordinateTransform tx = txFactory.createTransform(from, to);
        if (tx == null) {
            throw new IllegalArgumentException("Unable to find transform from " + from + " to " + to);
        }
        return tx;
    }

    private static class IdentityCoordinateTransform implements CoordinateTransform {

        @Override
        public CoordinateReferenceSystem getSourceCRS() {
            return null;
        }

        @Override
        public CoordinateReferenceSystem getTargetCRS() {
            return null;
        }

        @Override
        public ProjCoordinate transform(ProjCoordinate src, ProjCoordinate tgt) throws Proj4jException {
            return src;
        }
    
    }
}
