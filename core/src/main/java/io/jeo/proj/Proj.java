/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jeo.proj;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Pattern;

import io.jeo.geom.GeomBuilder;
import io.jeo.proj.wkt.ProjWKTEncoder;
import io.jeo.proj.wkt.ProjWKTParser;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.Proj4jException;
import org.osgeo.proj4j.ProjCoordinate;
import org.osgeo.proj4j.datum.Datum;
import org.osgeo.proj4j.io.Proj4FileReader;
import org.osgeo.proj4j.proj.Projection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    static Logger LOGGER = LoggerFactory.getLogger(Proj.class);

    static Pattern AUTH_CODE = Pattern.compile("\\w+:\\d+", Pattern.CASE_INSENSITIVE);

    static CRSFactory csFactory = new CRSFactory();
    static CoordinateTransformFactory txFactory = new CoordinateTransformFactory();
    static GeomBuilder gBuilder = new GeomBuilder();

    /** 
     * The canonical geographic coordinate reference system.
     */
    public static final CoordinateReferenceSystem EPSG_4326 = Proj.crs("EPSG:4326");

    /**
     * Google mercator
     */
    public static final CoordinateReferenceSystem EPSG_900913 = Proj.crs("EPSG:900913");

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
        if (s == null || s.isEmpty()) {
            return null;
        }
        if (!AUTH_CODE.matcher(s).matches()) {
            try {
                return crs(new String[]{s});
            }
            catch(RuntimeException e) {
                try {
                    return new ProjWKTParser().parse(s);
                }
                catch(Exception e2) {
                    throw e;
                }
            }
        }

        if ("epsg:4326".equalsIgnoreCase(s) && EPSG_4326 != null) {
            return EPSG_4326;
        }
        
        //hack for epsg:900913, we nweed to add this to proj4j
        if ("epsg:900913".equalsIgnoreCase(s)) {
            return EPSG_900913 != null ? EPSG_900913 : createFromExtra("epsg", "900913");
        }

        return csFactory.createFromName(s);
    }

    /**
     * Creates a crs object from projection parameter definition.
     * 
     * @param projdef The projection / proj4 parameters.
     * 
     * @return The crs object.
     */
    public static CoordinateReferenceSystem crs(String... projdef) {
        if (projdef != null && projdef.length == 1) {
            return csFactory.createFromParameters(null, projdef[0]);
        }
        return csFactory.createFromParameters(null, projdef);
    }

    /**
     * Returns the crs of the geometry, if it exists.
     *
     * @param geom The geometry object.
     *
     * @return The crs, or null.
     */
    public static CoordinateReferenceSystem crs(Geometry geom) {
        Object userData = geom.getUserData();
        if (userData instanceof CoordinateReferenceSystem) {
            return (CoordinateReferenceSystem) userData;
        }

        int srid = geom.getSRID();
        if (srid > 0) {
            return crs(srid);
        }

        return null;
    }

    /**
     * Sets the coordinate reference system of a geometry.
     *
     * @param g The geometry.
     * @param crs The crs.
     *
     * @return The original geometry object.
     * @see #crs(Geometry, CoordinateReferenceSystem, boolean)
     */
    public static Geometry crs(Geometry g, CoordinateReferenceSystem crs) {
        return crs(g, crs, true);
    }

    /**
     * Sets the coordinate reference system of a geometry.
     *
     * @param g The geometry.
     * @param crs The crs.
     * @param overwrite Whether to overwrite an existing crs that may exist.
     */
    public static Geometry crs(Geometry g, CoordinateReferenceSystem crs, boolean overwrite) {
        if (g != null) {
            if (g.getUserData() == null || (overwrite && g.getUserData() instanceof CoordinateReferenceSystem)) {
                g.setUserData(crs);
            }
            if (g.getSRID() == 0 || overwrite) {
                Integer srid = epsgCode(crs);
                if (srid != null) {
                    g.setSRID(srid);
                }
            }
        }
        return g;
    }

    private static CoordinateReferenceSystem createFromExtra(String auth, String code) {
        Proj4FileReader r = new Proj4FileReader();
        InputStream in = Proj.class.getResourceAsStream("other.extra");
        
        try {
            try {
                return csFactory.createFromParameters(
                    auth+":"+code, r.readParameters(code, in));
            }
            finally {
               in.close();
            }
        }
        catch(IOException e) {
            LOGGER.debug(String.format(Locale.ROOT,"Failure creating crs %s:%s from extra", auth, code));
            return null;
        }
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

            Point p1 = gBuilder.point(p.getMinLongitudeDegrees(), p.getMinLatitudeDegrees()).toPoint();
            Point p2 = gBuilder.point(p.getMaxLongitudeDegrees(), p.getMaxLatitudeDegrees()).toPoint();

            p1 = reproject(p1, geo, crs);
            p2 = reproject(p2, geo, crs);

            return new Envelope(p1.getX(), p2.getX(), p1.getY(), p2.getY());
        }

        return null;
    }

    /**
     * Reprojects a geometry object between two coordinate reference systems.
     * <p>
     * This method is convenience for:
     * <pre><code>
     *   reproject(g, crs(from), crs(to));
     * </code></pre>
     * </p>
     * 
     * @param g The geometry to reproject.
     * @param from The source crs, as defined by {@link #crs(String)}
     * @param to The target crs, as defined by {@link #crs(String)}
     * 
     * @return The reprojected geometry.
     * 
     * @see {@link #crs(String)}
     * @see {@link #reproject(Geometry, CoordinateReferenceSystem, CoordinateReferenceSystem)}
     */
    public static <T extends Geometry> T reproject(T g, String from, String to) {
        return reproject(g, crs(from), crs(to));
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

        return transform(g, transform(from, to));
    }

    /**
     * Transforms a geometry object using an explicit transformation.
     * 
     * @param g The geometry.
     * @param tx The transform to to apply.
     * 
     * @return The transformed geometry.
     */
    public static <T extends Geometry> T transform(T g, CoordinateTransform tx) {
        return transform(g, tx, false);
    }

    /**
     * Transforms a geometry object using an explicit transformation with the option to do an 
     * in place transformation.
     * <p>
     * When <tt>inPlace</tt> is set to true the geometry coordinates will be modified directly. When
     * set to false a clone of the geometry is made and modified. Since cloning a geometry can be a
     * very expensive operation doing an in place transform can be much more efficient but has the
     * downside of modifying the geometry directly.
     * </p>
     * 
     * @param g The geometry.
     * @param tx The transform to to apply.
     * 
     * @return The transformed geometry.
     */
    public static <T extends Geometry> T transform(T g, CoordinateTransform tx, boolean inPlace) {
        if (tx instanceof IdentityCoordinateTransform) {
            return g;
        }

        T h = inPlace ? g : (T) g.clone();
        h.apply((CoordinateSequenceFilter) new CoordinateTransformer(tx));
        return h;
    }

    /**
     * Reprojects an envelope between two coordinate reference systems.
     * <p>
     * This method is convenience for:
     * <pre><code>
     *   reproject(e, crs(from), crs(to));
     * </code></pre>
     * </p>
     * 
     * @param e The envelope to reproject.
     * @param from The source crs, as defined by {@link #crs(String)}
     * @param to The target crs, as defined by {@link #crs(String)}
     * 
     * @return The reprojected envelope.
     * 
     * @see {@link #crs(String)}
     * @see {@link #reproject(Envelope, CoordinateReferenceSystem, CoordinateReferenceSystem)}
     * 
     */
    public static Envelope reproject(Envelope e, String from, String to) {
        return reproject(e, crs(from), crs(to));
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
        
        CoordinateTransform tx = transform(from, to);
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

    public static CoordinateTransform transform(CoordinateReferenceSystem from, 
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

    /**
     * Returns the projection string for the specified CRS.
     */
    public static String toString(CoordinateReferenceSystem crs) {
        return crs.getParameterString();
    }

    /**
     * Creates a crs from Well Known Text. 
     * 
     * @param wkt WKT representation of a CRS.
     */
    public static CoordinateReferenceSystem fromWKT(String wkt) {
        try {
            return new ProjWKTParser().parse(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encodes a crs as Well Known Text.
     *  
     * @param crs The coordinate reference system.
     * @param format Whether to format the encoded result.
     * 
     */
    public static String toWKT(CoordinateReferenceSystem crs, boolean format) {
        return new ProjWKTEncoder().encode(crs, format);
    }

    /**
     * Determines if two crs objects are equal.
     * <p>
     * Two crs objects are considered equal if {@link Datum#isEqual(Datum)} returns <tt>true</tt> 
     * and the two {@link CoordinateReferenceSystem#getProjection()} instances are the same type.
     * </p>
     * 
     */
    public static boolean equal(CoordinateReferenceSystem crs1, CoordinateReferenceSystem crs2) {
        if (crs1 == crs2) {
            return true;
        }

        if (crs1 == null || crs2 == null) {
            return false;
        }

        Datum dat1 = crs1.getDatum();
        Datum dat2 = crs2.getDatum();

        if (!dat1.isEqual(dat2)) {
            return false;
        }

        Projection p1 = crs1.getProjection();
        Projection p2 = crs2.getProjection();

        // TODO: do a better job of this 
        return p1.getClass().equals(p2.getClass());
    }
}
