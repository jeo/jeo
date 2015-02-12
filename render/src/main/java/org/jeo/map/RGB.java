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
package org.jeo.map;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jeo.util.Interpolate;
import org.jeo.util.Optional;

/**
 * A color in RGBA color space.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class RGB {

    public static final RGB aliceblue = new RGB(240,248,255);
    public static final RGB antiquewhite = new RGB(250,235,215);
    public static final RGB aqua = new RGB(0,255,255);
    public static final RGB aquamarine = new RGB(127,255,212);
    public static final RGB azure = new RGB(240,255,255);
    public static final RGB beige = new RGB(245,245,220);
    public static final RGB bisque = new RGB(255,228,196);
    public static final RGB black = new RGB(0,0,0);
    public static final RGB blanchedalmond = new RGB(255,235,205);
    public static final RGB blue = new RGB(0,0,255);
    public static final RGB blueviolet = new RGB(138,43,226);
    public static final RGB brown = new RGB(165,42,42);
    public static final RGB burlywood = new RGB(222,184,135);
    public static final RGB cadetblue = new RGB(95,158,160);
    public static final RGB chartreuse = new RGB(127,255,0);
    public static final RGB chocolate = new RGB(210,105,30);
    public static final RGB coral = new RGB(255,127,80);
    public static final RGB cornflowerblue = new RGB(100,149,237);
    public static final RGB cornsilk = new RGB(255,248,220);
    public static final RGB crimson = new RGB(220,20,60);
    public static final RGB cyan = new RGB(0,255,255);
    public static final RGB darkblue = new RGB(0,0,139);
    public static final RGB darkcyan = new RGB(0,139,139);
    public static final RGB darkgoldenrod = new RGB(184,134,11);
    public static final RGB darkgray = new RGB(169,169,169);
    public static final RGB darkgreen = new RGB(0,100,0);
    public static final RGB darkkhaki = new RGB(189,183,107);
    public static final RGB darkmagenta = new RGB(139,0,139);
    public static final RGB darkolivegreen = new RGB(85,107,47);
    public static final RGB darkorange = new RGB(255,140,0);
    public static final RGB darkorchid = new RGB(153,50,204);
    public static final RGB darkred = new RGB(139,0,0);
    public static final RGB darksalmon = new RGB(233,150,122);
    public static final RGB darkseagreen = new RGB(143,188,143);
    public static final RGB darkslateblue = new RGB(72,61,139);
    public static final RGB darkslategray = new RGB(47,79,79);
    public static final RGB darkturquoise = new RGB(0,206,209);
    public static final RGB darkviolet = new RGB(148,0,211);
    public static final RGB deeppink = new RGB(255,20,147);
    public static final RGB deepskyblue = new RGB(0,191,255);
    public static final RGB dimgray = new RGB(105,105,105);
    public static final RGB dodgerblue = new RGB(30,144,255);
    public static final RGB firebrick = new RGB(178,34,34);
    public static final RGB floralwhite = new RGB(255,250,240);
    public static final RGB forestgreen = new RGB(34,139,34);
    public static final RGB fuchsia = new RGB(255,0,255);
    public static final RGB gainsboro = new RGB(220,220,220);
    public static final RGB ghostwhite = new RGB(248,248,255);
    public static final RGB gold = new RGB(255,215,0);
    public static final RGB goldenrod = new RGB(218,165,32);
    public static final RGB gray = new RGB(128,128,128);
    public static final RGB green = new RGB(0,128,0);
    public static final RGB greenyellow = new RGB(173,255,47);
    public static final RGB grey = new RGB(84,84,84);
    public static final RGB honeydew = new RGB(240,255,240);
    public static final RGB hotpink = new RGB(255,105,180);
    public static final RGB indianred = new RGB(205,92,92);
    public static final RGB indigo = new RGB(75,0,130);
    public static final RGB ivory = new RGB(255,255,240);
    public static final RGB khaki = new RGB(240,230,140);
    public static final RGB lavender = new RGB(230,230,250);
    public static final RGB lavenderblush = new RGB(255,240,245);
    public static final RGB lawngreen = new RGB(124,252,0);
    public static final RGB lemonchiffon = new RGB(255,250,205);
    public static final RGB lightblue = new RGB(173,216,230);
    public static final RGB lightcoral = new RGB(240,128,128);
    public static final RGB lightcyan = new RGB(224,255,255);
    public static final RGB lightgoldenrodyellow = new RGB(250,250,210);
    public static final RGB lightgrey = new RGB(211,211,211);
    public static final RGB lightgreen = new RGB(144,238,144);
    public static final RGB lightpink = new RGB(255,182,193);
    public static final RGB lightsalmon = new RGB(255,160,122);
    public static final RGB lightseagreen = new RGB(32,178,170);
    public static final RGB lightskyblue = new RGB(135,206,250);
    public static final RGB lightslategray = new RGB(119,136,153);
    public static final RGB lightsteelblue = new RGB(176,196,222);
    public static final RGB lightyellow = new RGB(255,255,224);
    public static final RGB lime = new RGB(0,255,0);
    public static final RGB limegreen = new RGB(50,205,50);
    public static final RGB linen = new RGB(250,240,230);
    public static final RGB magenta = new RGB(255,0,255);
    public static final RGB maroon = new RGB(128,0,0);
    public static final RGB mediumaquamarine = new RGB(102,205,170);
    public static final RGB mediumblue = new RGB(0,0,205);
    public static final RGB mediumorchid = new RGB(186,85,211);
    public static final RGB mediumpurple = new RGB(147,112,216);
    public static final RGB mediumseagreen = new RGB(60,179,113);
    public static final RGB mediumslateblue = new RGB(123,104,238);
    public static final RGB mediumspringgreen = new RGB(0,250,154);
    public static final RGB mediumturquoise = new RGB(72,209,204);
    public static final RGB mediumvioletred = new RGB(199,21,133);
    public static final RGB midnightblue = new RGB(25,25,112);
    public static final RGB mintcream = new RGB(245,255,250);
    public static final RGB mistyrose = new RGB(255,228,225);
    public static final RGB moccasin = new RGB(255,228,181);
    public static final RGB navajowhite = new RGB(255,222,173);
    public static final RGB navy = new RGB(0,0,128);
    public static final RGB oldlace = new RGB(253,245,230);
    public static final RGB olive = new RGB(128,128,0);
    public static final RGB olivedrab = new RGB(107,142,35);
    public static final RGB orange = new RGB(255,165,0);
    public static final RGB orangered = new RGB(255,69,0);
    public static final RGB orchid = new RGB(218,112,214);
    public static final RGB palegoldenrod = new RGB(238,232,170);
    public static final RGB palegreen = new RGB(152,251,152);
    public static final RGB paleturquoise = new RGB(175,238,238);
    public static final RGB palevioletred = new RGB(216,112,147);
    public static final RGB papayawhip = new RGB(255,239,213);
    public static final RGB peachpuff = new RGB(255,218,185);
    public static final RGB peru = new RGB(205,133,63);
    public static final RGB pink = new RGB(255,192,203);
    public static final RGB plum = new RGB(221,160,221);
    public static final RGB powderblue = new RGB(176,224,230);
    public static final RGB purple = new RGB(128,0,128);
    public static final RGB red = new RGB(255,0,0);
    public static final RGB rosybrown = new RGB(188,143,143);
    public static final RGB royalblue = new RGB(65,105,225);
    public static final RGB saddlebrown = new RGB(139,69,19);
    public static final RGB salmon = new RGB(250,128,114);
    public static final RGB sandybrown = new RGB(244,164,96);
    public static final RGB seagreen = new RGB(46,139,87);
    public static final RGB seashell = new RGB(255,245,238);
    public static final RGB sienna = new RGB(160,82,45);
    public static final RGB silver = new RGB(192,192,192);
    public static final RGB skyblue = new RGB(135,206,235);
    public static final RGB slateblue = new RGB(106,90,205);
    public static final RGB slategray = new RGB(112,128,144);
    public static final RGB snow = new RGB(255,250,250);
    public static final RGB springgreen = new RGB(0,255,127);
    public static final RGB steelblue = new RGB(70,130,180);
    public static final RGB tan = new RGB(210,180,140);
    public static final RGB teal = new RGB(0,128,128);
    public static final RGB thistle = new RGB(216,191,216);
    public static final RGB tomato = new RGB(255,99,71);
    public static final RGB turquoise = new RGB(64,224,208);
    public static final RGB violet = new RGB(238,130,238);
    public static final RGB wheat = new RGB(245,222,179);
    public static final RGB white = new RGB(255,255,255);
    public static final RGB whitesmoke = new RGB(245,245,245);
    public static final RGB yellow = new RGB(255,255,0);
    public static final RGB yellowgreen = new RGB(154,205,50);

    int r, g, b;
    int a = 255;

    public RGB(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public RGB(int red, int green, int blue, int alpha) {
        this.r = red;
        this.g = green;
        this.b = blue;
        this.a = alpha;
    }

    public RGB(String rgb) {
        parse(rgb);
    }

    public int getAlpha() {
        return a;
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public float getOpacity() {
        return (float) (getAlpha() / 255.0);
    }

    public RGB alpha(float opacity) {
        return alpha((int)(255 * opacity));
    }

    public RGB alpha(int alpha) {
        return new RGB(r, g, b, alpha);
    }

    /**
     * Creates a new color from hue, saturation, lightness (HSL) values.
     * 
     * @param h The hue value.
     * @param s The saturation value.
     * @param l The lightness value.
     * 
     * @return The new color.
     */
    public static RGB fromHSL(double h, double s, double l) {
        double r, g, b;

        if (s == 0) {
            //achromatic
            r = g = b = l;
        }
        else {
          double q = l < 0.5 ? l * (1+s) : l + s - l * s;
          double p = 2 * l - q;

          r = hueTorgb(p, q, h + 1/3.0);
          g = hueTorgb(p, q, h);
          b = hueTorgb(p, q, h - 1/3.0);
        }

        return new RGB((int)Math.round(255*r), (int)Math.round(255*g), (int)Math.round(255*b));
    }

    /**
     * Creates a new color from hue, saturation, lightness (HSL) values.
     *
     * @param hsl 3 element array of hue,saturation, and lightness values.
     *
     * @return The new color.
     */
    public static RGB fromHSL(double[] hsl) {
        if (hsl.length != 3) {
            throw new IllegalArgumentException("input must be array of length 3");
        }

        return fromHSL(hsl[0], hsl[1], hsl[2]);
    }

    static double hueTorgb(double p, double q, double t) {
        if (t < 0) {
            t += 1;
        }
        if (t > 1) {
            t -= 1;
        }
        if (t < 1/6.0) {
            return p + (q - p) * 6 * t;
        }
        if (t < 1/2.0) {
            return q;
        }
        if (t < 2/3.0) {
             return p + (q - p) * (2/3.0 - t) * 6;
        }
        return p;
    }

    /**
     * The hue, saturation, lightness (HSL) representation of the color.
     */
    public double[] hsl() {
        double r = this.r / 255.0;
        double g = this.g / 255.0;
        double b = this.b / 255.0;
        
        double lo = Math.min(Math.min(r,g), b);
        double hi = Math.max(Math.max(r,g), b);

        double h,s,l;
        h = s = l = (lo + hi) / 2.0;

        if (lo == hi) {
            // achromatic
            h = s = 0;;
        }
        else {
            double delta = hi - lo;
            s = l > 0.5 ? delta / (2-hi-lo) : delta / (hi+lo);

            if (hi == r) {
                h = (g-b)/delta + (g < b ? 6 : 0);
            }
            else if (hi == g) {
                h = (b-r) / delta + 2;
            }
            else {
                h = (r-g) / delta + 4;
            }

            h /= 6.0;
        }
        return new double[]{h,s,l}; 
    }

    /**
     * Returns the interpolated color value between this color and the specified color.
     *
     * @param other The other color.
     * @param amt Number between 0 and 1 inclusive.
     *
     * @return The interpolated value.
     */
    public RGB interpolate(RGB other, double amt) {
        if (amt < 0 || amt > 1) {
            throw new IllegalArgumentException("amount must be in range [0,1]");
        }

        double[] hsl1 = hsl();
        double[] hsl2 = other.hsl();
        double[] dhsl = sub(hsl2, hsl1, hsl2);

        return fromHSL(doInterpolate(hsl1, dhsl, amt, new double[3]));
    }

    /**
     * Interpolates a number of RGB values between this color and the specified color.
     * 
     * @param other The color to interpolate to.
     * @param n The number of values to interpolate.
     * @param method The interpolation method.
     * 
     * @return A set of <tt>n+1</tt> RGB values.
     */
    public List<RGB> interpolate(RGB other, int n, Interpolate.Method method) {
        double[] hsl1 = hsl();
        double[] hsl2 = other.hsl();
        double[] dhsl = sub(hsl2, hsl1, hsl2);

        Iterator<Double> alphas = Interpolate.interpolate(a, other.a, n, method).iterator();
        List<RGB> vals = new ArrayList<RGB>(n+1);
        double[] hsl = new double[3];
        for (Double d : Interpolate.interpolate(0, n, n, method)) {
            doInterpolate(hsl1, dhsl, d/((float)n), hsl);
            vals.add(fromHSL(hsl).alpha(alphas.next().intValue()));
        }

        return vals;
    }

    double[] doInterpolate(double[] hsl, double[] dhsl, double amt,  double[] result) {
        result[0] = hsl[0] + amt * dhsl[0];
        result[1] = hsl[1] + amt * dhsl[1];
        result[2] = hsl[2] + amt * dhsl[2];
        return result;
    }

    double[] sub(double[] d1, double[] d2, double[] res) {
        for (int i = 0; i < d1.length; i++) {
            res[i] = d1[i] - d2[i];
        }
        return res;
    }

    void parse(String rgb) {
        if (initFromName(rgb)) {
            return;
        }

        StringBuilder sb = new StringBuilder(rgb);

        if (rgb.startsWith("#")) {
            sb.delete(0, 1);
        }

        if (sb.length() == 3) {
            sb.insert(2, sb.charAt(2));
            sb.insert(1, sb.charAt(1));
            sb.insert(0, sb.charAt(0));
        }

        if (sb.length() == 8) {
            a = Integer.parseInt(sb.substring(0,2), 16);
            sb.delete(0, 2);
        }

        if (sb.length() != 6) {
            throw new IllegalArgumentException("Unable to parse " + rgb + " as RGB");
        }

        r = Integer.parseInt(sb.substring(0,2), 16);
        g = Integer.parseInt(sb.substring(2,4), 16);
        b = Integer.parseInt(sb.substring(4,6), 16);
    }

    boolean initFromName(String name) {
        try {
            Field f = getClass().getDeclaredField(name);
            if (Modifier.isStatic(f.getModifiers())) {
                Object obj = f.get(null);
                if (obj instanceof RGB) {
                    RGB rgb = (RGB) obj;
                    this.r = rgb.r;
                    this.g = rgb.g;
                    this.b = rgb.b;
                    this.a = rgb.a;
                    return true;
                }
            }
        } 
        catch (Throwable t) {
        }
        return false;
    }

    /**
     * The hex rgb string of this color.
     *
     * @return String of the format: <tt>#rrggbb</tt> 
     */
    public String rgbhex() {
        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * The hex rgba string of this color.
     *
     * @return String of the format: <tt>#rrggbbaa</tt> 
     */
    public String rgbahex() {
        return String.format("#%02x%02x%02x%02x", r, g, b, a);
    }

    @Override
    public String toString() {
        return new StringBuilder("RGB(").append(r).append(",").append(g).append(",").append(b)
            .append(",").append(a).append(")").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + a;
        result = prime * result + b;
        result = prime * result + g;
        result = prime * result + r;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RGB other = (RGB) obj;
        if (a != other.a)
            return false;
        if (b != other.b)
            return false;
        if (g != other.g)
            return false;
        if (r != other.r)
            return false;
        return true;
    }

    public static Optional<RGB> convert(Object obj) {
        if (obj == null) {
            return Optional.nil(RGB.class);
        }

        if (obj instanceof RGB) {
            return Optional.of((RGB)obj);
        }

        return Optional.of(new RGB(obj.toString()));
    }

}
