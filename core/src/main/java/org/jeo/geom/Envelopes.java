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
package org.jeo.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for {@link Envelope}.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class Envelopes {

    /**
     * World bounds for the canonical EPSG:4326, longitude/latitude ordering.
     */
    public static final Envelope WORLD_BOUNDS_4326 = new Envelope(-180,180,-90,90);

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
        return String.format(Locale.ROOT, "%f%s%f%s%f%s%f", e.getMinX(), delim, alt?e.getMinY():e.getMaxX(), delim,
            alt?e.getMaxX():e.getMinY(), delim, e.getMaxY());
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
     * @param alt Whether x/y components alternate, if <tt>true</tt> the <tt>str</tt> argument must
     * be of the form <tt>&lt;x1>,&lt;y1>,&lt;x2,&lt;y2></tt>. If <tt>false</tt> the <tt>str</tt>
     * argument must be of the form tt>&lt;x1>,&lt;x2>,&lt;y1,&lt;y2>.
     */
    public static Envelope parse(String str, boolean alt) {
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
    public static Envelope parse(String str, boolean alt, String delim) {
        String[] split = str.split(delim);
        if (split.length != 4) {
            return null;
        }

        double x1 = Double.parseDouble(split[0]);
        double y1 = Double.parseDouble(split[alt?1:2]);
        double x2 = Double.parseDouble(split[alt?2:1]);
        double y2 = Double.parseDouble(split[3]);

        return new Envelope(x1,x2,y1,y2);
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
}
