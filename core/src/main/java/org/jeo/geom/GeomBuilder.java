package org.jeo.geom;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

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

/**
 * Builder for geometry objects.
 * <p>
 * Example usage:
 * <pre><code>
 * GeometryBuilder gb = new GeometryBuilder();
 *
 * // create array two 2d points and turn into a line string
 * gb.points(1,2,3,4,5,6).toLineString();
 * 
 * // build a polygon with holes
 * gb.points(0,0,10,0,10,10,0,10,0,0).ring()
 *   .points(4,4,6,4,6,6,4,6,4,4).ring()
 *   .toPolygon();
 *   
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class GeomBuilder {

    GeometryFactory factory;
    Deque<Coordinate> cstack = new ArrayDeque<Coordinate>();
    Deque<Geometry> gstack = new ArrayDeque<Geometry>();

    /**
     * Constructs a builder with the default geometry factory.
     */
    public GeomBuilder() {
        this(new GeometryFactory());
    }

    /**
     * Constructs a builder with an explicit geometry factory.
     */
    public GeomBuilder(GeometryFactory factory) {
        this.factory = factory;
    }

    /**
     * Adds a 2d point to the coordinate stack. 
     */
    public GeomBuilder point(double x, double y) {
        cstack.push(new Coordinate(x, y));
        return this;
    }

    /**
     * Adds a 3d point to the coordinate stack. 
     */
    public GeomBuilder pointz(double x, double y, double z) {
        cstack.push(new Coordinate(x,y,z));
        return this;
    }

    /**
     * Adds an array of 2d points to the coordinate stack. 
     */
    public GeomBuilder points(double ...ord) {
        if (ord.length % 2 != 0) {
            throw new IllegalArgumentException("Must specify even number of ordinates");
        }

        for (int i = 0; i < ord.length; i +=2 ) {
            point(ord[i], ord[i+1]);
        }

        return this;
    }

    /**
     * Adds an array of 3d points to the coordinate stack. 
     */
    public GeomBuilder pointsz(double ...ord) {
        if (ord.length % 3 != 0) {
            throw new IllegalArgumentException("Must specify ordinates as triples");
        }

        for (int i = 0; i < ord.length; i +=3 ) {
            pointz(ord[i], ord[i+1], ord[i+2] );
        }

        return this;
    }

    /**
     * Creates a Point from the last point on the coordinate stack, and places the result
     * on the geometry stack.
     */
    public GeomBuilder point() {
        gstack.push(factory.createPoint(cpop()));
        return this;
    }

    /**
     * Creates a LineString from all points on the coordinate stack, and places the result
     * on the geometry stack.
     */
    public GeomBuilder lineString() {
        gstack.push(factory.createLineString(cpopAll()));
        return this;
    }

    /**
     * Creates a LinearRing from all points on the coordinate stack, and places the result
     * on the geometry stack.
     */
    public GeomBuilder ring() {
        gstack.push(factory.createLinearRing(cpopAll()));
        return this;
    }

    /**
     * Creates a Polygon from all LinearRings on the geometry stack and places the result back 
     * on the geometry stack.
     */
    public GeomBuilder polygon() {
        LinearRing[] rings = gpopAll(LinearRing.class);
        LinearRing outer = rings[0];
        LinearRing[] inner = null;
        if (rings.length > 1) {
            inner = Arrays.copyOfRange(rings, 1, rings.length);
        }

        gstack.push(factory.createPolygon(outer, inner));
        return this;
    }

    /**
     * Creates a MultiPoint from all coordinates on the coordinate stack, plaching the result
     * back on the geometry stack. 
     * <p>
     * If the coordinate stack is empty this method will consume all Point geometries on 
     * the geometry stack.
     * </p>
     */
    public GeomBuilder multiPoint() {
        if (!cstack.isEmpty()) {
            gstack.push(factory.createMultiPoint(cpopAll()));
        }
        else {
            gstack.push(factory.createMultiPoint(gpopAll(Point.class)));
        }
        return this;
    }

    /**
     * Creates a MultiLineString from all LineStrings on the geometry stack and places the result 
     * back on the geometry stack.
     */
    public GeomBuilder multiLineString() {
        gstack.push(factory.createMultiLineString(gpopAll(LineString.class)));
        return this;
    }

    /**
     * Creates a MultiPolygon from all Polygons on the geometry stack and places the result 
     * back on the geometry stack.
     */
    public GeomBuilder multiPolygon() {
        gstack.push(factory.createMultiPolygon(gpopAll(Polygon.class)));
        return this;
    }

    /**
     * Creates a GeometryCollection from all Geometries on the geometry stack and places the result 
     * back on the geometry stack.
     */
    public GeomBuilder collection() {
        gstack.push(factory.createGeometryCollection(gpopAll(Geometry.class)));
        return this;
    }

    /**
     * Buffers the geometry at the top of the geometry stack, and places the result back on the 
     * geometry stack.
     */
    public GeomBuilder buffer(double amt) {
        gstack.push(gpop(Geometry.class).buffer(amt));
        return this;
    }

    /**
     * Consumes the top of the geometry stack. 
     */
    public Geometry get() {
        return gpop(Geometry.class);
    }

    /**
     * Builds and returns a Point.
     * <p>
     * This method is equivalent to:
     * <pre>
     *   (Point) point().get();
     * </pre>
     * </p>
     */
    public Point toPoint() {
        return point().gpop(Point.class);
    }

    /**
     * Builds and returns a LineString.
     * <p>
     * This method is equivalent to:
     * <pre>
     *   (LineString) lineString().get();
     * </pre>
     * </p>
     */
    public LineString toLineString() {
        return lineString().gpop(LineString.class);
    }

    /**
     * Builds and returns a Polygon.
     * <p>
     * This method is equivalent to:
     * <pre>
     *   (Polygon) polygon().get();
     * </pre>
     * </p>
     */
    public Polygon toPolygon() {
        return polygon().gpop(Polygon.class);
    }

    /**
     * Builds and returns a MultiPoint.
     * <p>
     * This method is equivalent to:
     * <pre>
     *   (MultiPoint) multiPoint().get();
     * </pre>
     * </p>
     */
    public MultiPoint toMultiPoint() {
        return multiPoint().gpop(MultiPoint.class);
    }

    /**
     * Builds and returns a MultiLineString.
     * <p>
     * This method is equivalent to:
     * <pre>
     *   (MultiLineString) multiLineString().get();
     * </pre>
     * </p>
     */
    public MultiLineString toMultiLineString() {
        return multiLineString().gpop(MultiLineString.class);
    }

    /**
     * Builds and returns a MultiPolygon.
     * <p>
     * This method is equivalent to:
     * <pre>
     *   (MultiPolygon) multiPolygon().get();
     * </pre>
     * </p>
     */
    public MultiPolygon toMultiPolygon() {
        return multiPolygon().gpop(MultiPolygon.class);
    }

    /**
     * Builds and returns a GEometryCollection.
     * <p>
     * This method is equivalent to:
     * <pre>
     *   (GeometryCollection) collection().get();
     * </pre>
     * </p>
     */
    public GeometryCollection toCollection() {
        return collection().gpop(GeometryCollection.class);
    }

    Coordinate cpop() {
        return cpop(1)[0];
    }

    Coordinate[] cpop(int n) {
        if (cstack.size() < n) {
            throw new IllegalStateException(String.format("Expected %d values on coordinate stack, " 
                + "but found %d", n, cstack.size()));
        }

        Coordinate[] c = new Coordinate[n];
        for (int i = 0; i < n; i++) {
            c[n-i-1] = cstack.pop();
        }
        return c;
    }

    Coordinate[] cpopAll() {
        if (cstack.isEmpty()) {
            throw new IllegalStateException("Coordinate stack is empty");
        }

        return cpop(cstack.size());
    }

    <T extends Geometry> T gpop(Class<T> clazz) {
        return gpop(1, clazz)[0];
    }

    <T extends Geometry> T[] gpop(int n, Class<T> clazz) {
        if (gstack.size() < n) {
            throw new IllegalStateException(String.format("Expected %d values on geometry stack, "
                + "but found %d", n, gstack.size()));
        }

        T[] l = (T[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) {
            Object g = gstack.pop();
            if (!clazz.isInstance(g)) {
                throw new IllegalStateException(String.format("Expected %s on geometry stack, but "
                    + "found %s", clazz.getSimpleName(), g.getClass().getSimpleName()));
            }

            l[n-i-1] = clazz.cast(g);
        }
        return l;
    }

    <T extends Geometry> T[] gpopAll(Class<T> clazz) {
        if (gstack.isEmpty()) {
            throw new IllegalArgumentException("Geometry stack is empty");
        }

        int n = 0;
        Iterator<Geometry> it = gstack.iterator();
        while (it.hasNext() && clazz.isInstance(it.next())) {
            n++;
        }

        if (n == 0) {
            throw new IllegalArgumentException(
                String.format("Expected %s on geometry stack", clazz.getSimpleName())); 
        }

        return gpop(n, clazz);
    }
}

