package io.jeo.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Extension of {@link Envelope} providing additional utility.
 */
public class Bounds extends Envelope {

    /**
     * World bounds for the canonical EPSG:4326, longitude/latitude ordering.
     */
    public static final Bounds WORLD_BOUNDS_4326 = new Bounds(-180,180,-90,90);

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
    public static Bounds scale(Envelope env, double scale) {
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
    public static Bounds scale(Envelope env, double scale, Coordinate focus) {
        checkContains(env, focus);
        checkPositive(scale);

        double minx = focus.x -  (focus.x - env.getMinX()) * scale;
        double maxx = focus.x + (env.getMaxX() - focus.x) * scale;
        double miny = focus.y - (focus.y - env.getMinY()) * scale;
        double maxy = focus.y + (env.getMaxY() - focus.y) * scale;

        return new Bounds(minx, maxx, miny, maxy);
    }

    /**
     * Expands an envelope.
     *
     * @param env The envelope to expand.
     * @param x The x amount to expand by.
     * @param y The y amount to expand by.
     *
     * @return The expanded envelope.
     */
    public static Bounds expand(Envelope env, double x, double y) {
        Bounds b = new Bounds(env);
        b.expandBy(x, y);
        return b;
    }

    /**
     * Adjusts the envelope to have a square aspect ratio, expanding vertically/horizontally
     * as required.
     *
     * @param env The envelope to expand.
     *
     * @return The square envelope.
     */
    public static Bounds square(Envelope env) {
        double d = env.getWidth() - env.getHeight();
        return d > 0 ? expand(env, 0, d/2.0) :  expand(env, -d/2.0, 0);
    }

    /**
     * Returns a list of the 4 corners of the envelope, starting with the lower left
     * corner and continuing clockwise to the lower right.
     *
     * @param env the envelope.
     *
     * @return List of 4 corners.
     */
    public static List<Coordinate> corners(Envelope env) {
        List<Coordinate> l = new ArrayList<>();
        l.add(new Coordinate(env.getMinX(), env.getMinY()));
        l.add(new Coordinate(env.getMinX(), env.getMaxY()));
        l.add(new Coordinate(env.getMaxX(), env.getMaxY()));
        l.add(new Coordinate(env.getMaxX(), env.getMinY()));
        return l;
    }

    /**
     * Partitions the envelope into smaller envelopes.
     * <p>
     * The order of tiles returns starts from the lower left corner and proceeds in column-major
     * order.
     * </p>
     * <p>
     * The <tt>reuse</tt> parameter can be used to prevent a new envelope being created at each iteration. This should
     * only be used when envelopes from the iterator will not be modified.
     * </p>
     *
     * @param env The envelope to tile.
     * @param resx The horizontal resolution to tile at, in the range of (0,1].
     * @param resy The vertical resolution to tile at, in the range of (0,1].
     * @param reuse Optional envelope instance to reuse during calculation.
     *
     * @return Iterable of new envelopes.
     */
    public static <T extends Envelope> Iterable<T> tile(final T env, double resx, double resy, final T reuse) {
        final double dx = env.getWidth() * resx;
        final double dy = env.getHeight() * resy;

        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    double x = env.getMinX();
                    double y = env.getMinY();

                    @Override
                    public boolean hasNext() {
                        return x < env.getMaxX() && y < env.getMaxY();
                    }

                    @Override
                    public T next() {
                        T b = reuse != null ? reuse : (T) new Bounds();
                        b.init(x, Math.min(x+dx, env.getMaxX()), y, Math.min(y+dy, env.getMaxY()));

                        x += dx;
                        if (x >= env.getMaxX()) {
                            x = env.getMinX();
                            y += dy;
                        }

                        return b;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
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
    public static Bounds translate(Envelope env, double dx, double dy) {
        return new Bounds(env.getMinX()+dx, env.getMaxX()+dx, env.getMinY()+dy, env.getMaxY()+dy);
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
        return toString(e, ",", true);
    }

    /**
     * Encodes an envelope as a string with the specified delimiter and flag controlling order.
     * <p>
     *     When <tt>alt</tt> is true the order is <tt>&lt;x1>,&lt;y1>,&lt;x2,&lt;y2></tt>, when false
     *     the order is <tt>&lt;x1>,&lt;x2>,&lt;y1,&lt;y2></tt>
     * </p>
     */
    public static String toString(Envelope e, String delim, boolean alt) {
        return String.format(Locale.ROOT, "%f%s%f%s%f%s%f", e.getMinX(), delim, alt ? e.getMinY() : e.getMaxX(), delim,
            alt ? e.getMaxX() : e.getMinY(), delim, e.getMaxY());
    }

    /**
     * Parses a string of the form <tt>&lt;x1>,&lt;y1>,&lt;x2,&lt;y2></tt> into an envelope.
     * <p>
     * To parse an envelope of the form <tt>&lt;x1>,&lt;x2>,&lt;y1,&lt;y2></tt> call
     *  <tt>parse(str, false)</tt>
     * </p>
     */
    public static Bounds parse(String str) {
        return parse(str, true);
    }

    /**
     * Parses a string into an envelope.
     *
     * @param str The envelope string.
     * @param alt Whether x/y components alternate, if <tt>true</tt> the <tt>str</tt> argument must
     * be of the form <tt>&lt;x1>,&lt;y1>,&lt;x2,&lt;y2></tt>. If <tt>false</tt> the <tt>str</tt>
     * argument must be of the form tt>&lt;x1>,&lt;x2>,&lt;y1,&lt;y2>.
     */
    public static Bounds parse(String str, boolean alt) {
        return parse(str, alt, "\\s*,\\s*");
    }

    /**
     * Parses a string into an envelope.
     *
     * @param str The envelope string.
     * @param alt Whether x/y components alternate, if <tt>true</tt> the <tt>str</tt> argument must
     * be of the form <tt>&lt;x1>,&lt;y1>,&lt;x2,&lt;y2></tt>. If <tt>false</tt> the <tt>str</tt>
     * argument must be of the form tt>&lt;x1>,&lt;x2>,&lt;y1,&lt;y2>.
     * @param delim Delimiter for x/y components.
     */
    public static Bounds parse(String str, boolean alt, String delim) {
        String[] split = str.split(delim);
        if (split.length != 4) {
            return null;
        }

        double x1 = Double.parseDouble(split[0]);
        double y1 = Double.parseDouble(split[alt?1:2]);
        double x2 = Double.parseDouble(split[alt?2:1]);
        double y2 = Double.parseDouble(split[3]);

        return new Bounds(x1,x2,y1,y2);
    }

    /**
     * Flips the x/y axis of the envelope.
     *
     * @return The new envelope.
     */
    public static Envelope flip(Envelope e) {
        return new Envelope(e.getMinY(), e.getMaxY(), e.getMinX(), e.getMaxX());
    }

    /**
     * Generates a random envelope at the specified resolution and constrained by
     * the specified bounds.
     *
     * @param bbox Envelope containing the randomly generated envelope.
     * @param res Resolution of containing envelope to generate random envelope at, in the exclusive range (0,1).
     *
     * @return The randomly generated envelope.
     */
    public static Envelope random(Envelope bbox, float res) {
        if (!(res > 0f && res < 1f)) {
            throw new IllegalArgumentException("res must be in range (0,1)");
        }
        double w = bbox.getWidth() * res;
        double h = bbox.getHeight() * res;

        double maxx = bbox.getMaxX();
        double maxy = bbox.getMaxY();

        double x = maxx;
        double y = maxy;
        while ((x + w > maxx) || (y + h > maxy)) {
            x = bbox.getMinX() + w*Math.random();
            y = bbox.getMinY() + h*Math.random();
        }

        return new Envelope(x, x+w, y, y+h);
    }

    /**
     * Generates a set of random bounding box constrained by area and resolution.
     *
     * @param bbox Envelope containing the randomly generated envelope.
     * @param minRes Minimum resolution constraint.
     * @param maxRes Maximum resolution constraint.
     * @param n Number of bounding boxes to generate.
     *
     * @see #random(com.vividsolutions.jts.geom.Envelope, float)
     */
    public static List<Envelope> randoms(Envelope bbox, float minRes, float maxRes, int n) {
        List<Envelope> list = new ArrayList<Envelope>(n);
        for (int i = 0; i < n; i++) {
            float r = 0;
            do {
                r = (float)(minRes + Math.random()*(maxRes - minRes));
            }
            while(!(r > 0 && r < 1));

            list.add(random(bbox, r));
        }
        return list;
    }

    static void checkContains(Envelope e, Coordinate c) {
        if (!e.contains(c)) {
            throw new IllegalArgumentException(
                String.format(Locale.ROOT,"Coordinate (%f, %f) not contained within %s", c.x, c.y, e));
        }
    }

    static void checkPositive(double scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,"scale %f not positive", scale));
        }
    }

    /**
     * Creates a new empty bounds.
     */
    public Bounds() {
    }

    /**
     * Creates a new bounds from an existing envelope.
     */
    public Bounds(Envelope env) {
        super(env);
    }

    /**
     * Creates a bounds from west, east, south, north values.
     *
     * @param west The westmost value on the horizontal axis, ie minX.
     * @param east The eastmost value on the horizontal axis, ie maxX.
     * @param south The southmost value on the vertical axis, ie minY.
     * @param north The northmost value on the vertical axis, ie maxY.
     */
    public Bounds(double west, double east, double south, double north) {
        super(west, east, south, north);
    }

    /**
     * Returns the left/west ordinate of the bounds.
     * <p>
     *  Synonym for {@link #getMinX()}.
     * </p>
     */
    public double west() {
        return getMinX();
    }

    /**
     * Returns the bottom/south ordinate of the bounds.
     * <p>
     *  Synonym for {@link #getMinY()}.
     * </p>
     */
    public double south() {
        return getMinY();
    }

    /**
     * Returns the right/east ordinate of the bounds.
     * <p>
     *  Synonym for {@link #getMaxX()}.
     * </p>
     */
    public double east() {
        return getMaxX();
    }

    /**
     * Returns the top/north ordinate of the bounds.
     * <p>
     *  Synonym for {@link #getMaxY()}.
     * </p>
     */
    public double north() {
        return getMaxY();
    }

    /**
     * Width of the bounds.
     */
    public double width() {
        return getWidth();
    }

    /**
     * Height of the bounds.
     */
    public double height() {
        return getHeight();
    }

    /**
     * Scales the bounds by a specified factor.
     *
     * @return A new Bounds instance.
     * @see {@link Bounds#scale(Envelope, double)}
     */
    public Bounds scale(double factor) {
        return scale(this, factor);
    }

    /**
     * Expands the bounds by the same amount along both axis.
     *
     * @param amt The amount to expand.
     *
     * @return A new Bounds instance.
     */
    public Bounds expand(double amt) {
        return expand(amt, amt);
    }

    /**
     * Expands the bounds along both axis.
     *
     * @param x Amount to expand along x axis.
     * @param y Amount to expand along y axis.
     *
     * @return A new instance.
     * @see Bounds#expand(Envelope, double, double)
     */
    public Bounds expand(double x, double y) {
        return expand(this, x, y);
    }

    /**
     * Adjusts the bounds to have a square aspect ratio, expanding vertically/horizontally
     * as required.
     *
     * @return A new Bounds instance.
     * @see Bounds#square(Envelope)
     */
    public Bounds square() {
        return square(this);
    }

    /**
     * Translates the bounds.
     *
     * @param dx The horizontal shift.
     * @param dy The vertical shift.
     *
     * @see Bounds#translate(Envelope, double, double)
     * @see Envelope#translate(double, double)
     */
    public Bounds shift(double dx, double dy) {
        return Bounds.translate(this, dx, dy);
    }

    /**
     * Returns a list of the 4 corners of the bounds, starting with the lower left
     * corner and continuing clockwise to the lower right.
     *
     * @return List of 4 corners.
     */
    public List<Coordinate> corners() {
        return corners(this);
    }

    /**
     * Partitions the bounds into smaller components.
     * <p>
     *  Example of tiling a bounds into 4 smaller bounds:
     *  <pre><code>
     *  Bounds b = new Bounds(0,10,0,10);
     *  b.tile(0.5, 0.5);
     *  </code></pre>
     * </p>
     * @param resx The horizontal resolution to tile at, in the range of (0,1].
     * @param resy The vertical resolution to tile at, in the range of (0,1].
     * @return
     */
    public Iterable<Bounds> tile(double resx, double resy) {
        return tile(this, resx, resy, null);
    }

    /**
     * Returns the bounds as a {@link Polygon}.
     */
    public Polygon polygon() {
        return toPolygon(this);
    }

    @Override
    public Bounds intersection(Envelope env) {
        return new Bounds(super.intersection(env));
    }

    @Override
    public String toString() {
        return String.format("(%f, %f, %f, %f)", west(), south(), east(), north());
    }
}
