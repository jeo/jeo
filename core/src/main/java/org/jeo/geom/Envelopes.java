package org.jeo.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility class for {@link Envelope}.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class Envelopes {

    /**
     * Scales an envelope around its center coordinate. 
     * <p>
     * A <tt>scale</tt> < 1 will shrink the envelope, a <tt>scale</tt> > 1 will grow the envelope.
     * The <tt>scale</tt> must be a positive value.
     * </p>
     *
     * @param env The envelope to scale.
     * @param scale The scale factor. 
     * 
     * @return The scaled enveloped.
     * 
     * @throws IllegalArgumentException If <tt>scale</tt> is not >= 0.
     */
    public static Envelope scale(Envelope env, double scale) {
        return scale(env, scale, env.centre());
    }

    /**
     * Scales an envelope around a coordinate contained within the envelope. 
     * <p>
     * A <tt>scale</tt> < 1 will shrink the envelope, a <tt>scale</tt> > 1 will grow the envelope.
     * The <tt>scale</tt> must be a positive value.
     * </p>
     *
     * @param env The envelope to scale.
     * @param scale The scale factor. 
     * @param focus The coordinate within the envelope to scale around.
     * 
     * @return The scaled enveloped.
     * 
     * @throws IllegalArgumentException If <tt>focus</tt> is not contained with <tt>env</tt>.
     * @throws IllegalArgumentException If <tt>scale</tt> is not >= 0.
     */
    public static Envelope scale(Envelope env, double scale, Coordinate focus) {
        checkContains(env, focus);
        checkPositive(scale);

        double minx = focus.x -  (focus.x - env.getMinX()) * scale;
        double maxx = focus.x + (env.getMaxX() - focus.x) * scale;
        double miny = focus.y - (focus.y - env.getMinY()) * scale;
        double maxy = focus.y + (env.getMaxY() - focus.y) * scale;

        return new Envelope(minx, maxx, miny, maxy);
    }

    /**
     * Translates an envelope along the x/y axis.
     * 
     * @param env The envelope to translate.
     * @param dx The horizontal / x-axis displacement. 
     * @param dy The vertical / y-axis displacement.
     * 
     * @return The translated envelope.
     */
    public static Envelope translate(Envelope env, double dx, double dy) {
        return new Envelope(env.getMinX()+dx, env.getMaxX()+dx, env.getMinY()+dy, env.getMaxY()+dy);
    }

    /**
     * Converts the envelope to a Polygon.
     */
    public static Polygon toPolygon(Envelope e) {
        return new GeomBuilder().points(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMinY(),
            e.getMaxX(), e.getMaxY(), e.getMinX(), e.getMaxY(), e.getMinX(), e.getMinY()).ring()
            .toPolygon();
    }

    /**
     * Checks if the envelope is null.
     * <p>
     * This method returns true if the reference is <code>null</code> or {@link Envelope#isNull()}
     * returns <code>true</code>.
     * </p>
     */
    public static boolean isNull(Envelope e) {
        return e == null || e.isNull();
    }

    /**
     * Encodes an envelope as a string of the form <tt>&lt;x1>,&lt;y1>,&lt;x2,&lt;y2></tt>.
     */
    public static String toString(Envelope e) {
        return String.format("%f,%f,%f,%f", e.getMinX(),e.getMinY(),e.getMaxX(),e.getMaxY());
    }

    /**
     * Parses a string of the form <tt>&lt;x1>,&lt;y1>,&lt;x2,&lt;y2></tt> into an envelope.
     * <p>
     * To parse an envelope of the form <tt>&lt;x1>,&lt;x2>,&lt;y1,&lt;y2></tt> call 
     *  <tt>parse(str, false)</tt>
     * </p>
     */
    public static Envelope parse(String str) {
        return parse(str, true);
    }

    /**
     * Parses a string into an envelope.
     * 
     * @param str The envelope string.
     * @param alt Whether x/y compontents alternate, if <tt>true</tt> the <tt>str</tt> argument must
     * be of the form <tt>&lt;x1>,&lt;y1>,&lt;x2,&lt;y2></tt>. If <tt>false</tt> the <tt>str</tt>
     * argument must be of the form tt>&lt;x1>,&lt;x2>,&lt;y1,&lt;y2>.
     */
    public static Envelope parse(String str, boolean alt) {
        String[] split = str.split(",");
        if (split.length != 4) {
            return null;
        }

        double x1 = Double.parseDouble(split[0]);
        double y1 = Double.parseDouble(split[alt?1:2]);
        double x2 = Double.parseDouble(split[alt?2:1]);
        double y2 = Double.parseDouble(split[3]);

        return new Envelope(x1,x2,y1,y2);
    }

    static void checkContains(Envelope e, Coordinate c) {
        if (!e.contains(c)) {
            throw new IllegalArgumentException(
                String.format("Coordinate (%f, %f) not contained within %s", c.x, c.y, e));
        }
    }

    static void checkPositive(double scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(String.format("scale %f not positive", scale));
        }
    }
}
