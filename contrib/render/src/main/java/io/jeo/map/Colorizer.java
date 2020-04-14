/* Copyright 2014 The jeo project. All rights reserved.
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
package io.jeo.map;

import io.jeo.filter.Expression;
import io.jeo.filter.Function;
import io.jeo.filter.Literal;
import io.jeo.filter.Mixed;
import io.jeo.util.Convert;
import io.jeo.util.Interpolate;
import io.jeo.util.Pair;

import java.util.*;

/**
 * Maps numeric values to colors.
 * <p>
 * A colorizer is composed of levels known as "stops" that define a lookup
 * table composed of ranges of numeric values. Each range maps to a color
 * value with an optional "mode" that controls how the mapping occurs.
 * </p>
 * <p>
 * Note: Currently only {@link Mode#DISCRETE} is supported. TODO: fix this.
 * </p>
 */
public class Colorizer {

    /**
     * Stop matching mode.
     *
     * @see {@linkplain https://github.com/mapnik/mapnik/wiki/RasterColorizer#modes}
     */
    public static enum Mode {
        /**
         * Causes all input values from the stops value, up until the next stops value.
         */
        DISCRETE,
        /**
         * Causes all input values from the stops value, up until the next stops value to be translated to a color
         * which is linearly interpolated between the two stops colors
         */
        LINEAR,

        /**
         * Exact causes an input value which matches the stops value to be translated to the stops color.
         */
        EXACT;
    }

    /**
     * Default default mode.
     */
    static final Mode DEFAULT_MODE = Mode.DISCRETE;

    /**
     * Default mode.
     */
    Mode mode = DEFAULT_MODE;

    /**
     * Default color.
     */
    RGB color = RGB.black;

    /**
     * Stops.
     */
    List<Stop> stops = new ArrayList<Stop>();

    /**
     * The default mode for the colorizer.
     */
    public Mode mode() {
        return mode;
    }

    /**
     * The default color for the colorizer.
     */
    public RGB color() {
        return color;
    }

    /**
     * List of stops in the colorizer.
     */
    public List<Stop> stops() {
        return stops;
    }

    /**
     * Maps a value to a color based on the stops of the colorizer.
     *
     * @param value The value to map.
     *
     * @return The mapped color.
     */
    public RGB map(Double value) {
        if (value == null || value.isNaN() || stops.isEmpty()) {
            return color;
        }

        Iterator<Stop> it = stops.iterator();
        Stop curr = null, next = null;
        do {
            next = it.next();
            if (next.value > value) {
                break;
            }

            curr = next;
            next = null;
        }
        while (it.hasNext());

        if (curr == null) {
            return color;
        }

        switch(curr.mode) {
            case DISCRETE:
                return curr.color;
            case LINEAR:
                if (next != null) {
                    double amt = (value-curr.value)/(next.value - curr.value);
                    return curr.color.interpolate(next.color, amt);
                }
                return curr.color;
            case EXACT:
                if (Math.abs(value.doubleValue() - curr.value) < curr.epsilon) {
                    return curr.color;
                }
                return color;
        }

        return color;
    }

    public Rule rule() {
        return encode(this, new Rule());
    }

    /**
     * The stop class.
     */
    public static class Stop {
        public final Double value;
        public final RGB color;
        public final Mode mode;
        public final Double epsilon;

        public Stop(double value, RGB color, Mode mode, Double epsilon) {
            this.value = value;
            this.color = color;
            this.mode = mode;
            this.epsilon = epsilon;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "stop(%.2f, %s, %s, %.2f)", value, color, mode, epsilon);
        }
    }

    /**
     * Encodes a colorizer into a style Rule.
     */
    public static Rule encode(Colorizer c, Rule rule) {
        rule.put("raster-colorizer-default-mode", c.mode);
        rule.put("raster-colorizer-default-color", c.color);

        Mixed mixed = new Mixed();
        for (Colorizer.Stop stop : c.stops()) {
            Function f = new Function("stop") {
                @Override
                public Object evaluate(Object obj) {
                    return null;
                }
            };
            f.args().add(new Literal(stop.value));
            f.args().add(new Literal(stop.color));
            f.args().add(new Literal(stop.mode));
            f.args().add(new Literal(stop.epsilon));
            mixed.expressions().add(f);
        }

        rule.put("raster-colorizer-stops", mixed);
        return rule;
    }

    /**
     * Decodes a colorizer from a style Rule.
     */
    public static Colorizer decode(Rule rule) {
        Colorizer.Builder cb = Colorizer.build();

        cb.mode(Colorizer.Mode.valueOf(
                rule.string(null, "raster-colorizer-default-mode", "linear").toUpperCase(Locale.ROOT)));
        cb.color(rule.color(null, "raster-colorizer-default-color", RGB.black));

        if (rule.has("raster-colorizer-stops")) {
            //TODO: make this routine more robust
            Mixed m = (Mixed) rule.get("raster-colorizer-stops");
            for (Expression expr : m.expressions()) {
                Function stop = (Function) expr;
                List<Expression> args = stop.args();
                Double value =  Convert.toNumber(args.get(0).evaluate(null)).get().doubleValue();
                RGB color = RGB.convert(args.get(1).evaluate(null)).get();
                if (args.size() > 2) {
                    Mode mode = Mode.valueOf(args.get(2).evaluate(null).toString().toUpperCase(Locale.ROOT));
                    if (args.size() > 3) {
                        double epsilon = Convert.toNumber(args.get(3).evaluate(null), Double.class).get();
                        if (epsilon != 0d) {
                            cb.stop(value, color, epsilon);
                        }
                        else {
                            cb.stop(value, color, mode);
                        }
                    }
                    else {
                        cb.stop(value, color, mode);
                    }
                }
                else {
                    cb.stop(value, color);
                }
            }
        }

        return cb.colorizer();
    }

    /**
     * Creates a new Colorizer builder.
     */
    public static Builder build() {
        return new Builder();
    }

    /**
     * Builder class for colorizer.
     */
    public static class Builder {

        Colorizer colorizer = new Colorizer();

        TreeSet<Stop> stops = new TreeSet<Stop>(new Comparator<Stop>() {
            @Override
            public int compare(Stop stop1, Stop stop2) {
            return stop1.value.compareTo(stop2.value);
            }
        });

        /**
         * Sets the default color for the colorizer.
         */
        public Builder color(RGB color) {
            colorizer.color = color;
            return this;
        }

        /**
         * Sets the default mode for the colorizer.
         */
        public Builder mode(Mode mode) {
            colorizer.mode = mode;
            return this;
        }

        /**
         * Adds a new stop to the colorizer using the default mode.
         */
        public Builder stop(Double value, RGB color) {
            return stop(value, color, DEFAULT_MODE);
        }

        /**
         * Adds a new stop to the colorizer using the specified mode.
         */
        public Builder stop(Double value, RGB color, Mode mode) {
            stops.add(new Stop(value, color, mode, 0d));
            return this;
        }

        /**
         * Adds a new stop to the colorizer using exact mode.
         * <p>
         * The <tt>epsilon</tt> parameter is used to create "fuzzy" match. The stop will match values in the range
         * [stop.value, stop.value+epsilon)
         * </p>
         */
        public Builder stop(Double value, RGB color, double epsilon) {
            stops.add(new Stop(value, color, Mode.EXACT, epsilon));
            return this;
        }

        /**
         * Creates a number of stop values based on linear interpolation between two value/color pairs.
         *
         * @param low The low color/value pair.
         * @param high The high color/value pair.
         * @param n The number of interpolation buckets, will product n+1 values.
         */
        public Builder interpolate(Pair<Double,RGB> low, Pair<Double,RGB> high, int n) {
            List<Double> values = Interpolate.linear(low.first, high.first, n);
            List<RGB> colors = low.second.interpolate(high.second, n, Interpolate.Method.LINEAR);

            for (int i = 0; i < values.size(); i++) {
                stop(values.get(i), colors.get(i));
            }

            return this;
        }

        /**
         * Returns the build colorizer.
         */
        public Colorizer colorizer() {
            colorizer.stops.clear();
            colorizer.stops.addAll(new ArrayList<Stop>(stops));
            return colorizer;
        }
    }
}
