package org.jeo.map;

/**
 * A color in RGBA color space.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class RGB {

    public static final RGB White = new RGB(255, 255, 255);
    public static final RGB Black = new RGB(0, 0, 0);
    public static final RGB Red = new RGB(255, 0, 0);
    public static final RGB Green = new RGB(0, 255, 0);
    public static final RGB Blue = new RGB(0, 0, 255);
    public static final RGB Gray = new RGB(128, 128, 128);

    int red, green, blue;
    int alpha = 255;

    public RGB(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public RGB(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public RGB(String rgb) {
        parse(rgb);
    }

    public int getAlpha() {
        return alpha;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public float getOpacity() {
        return (float) (getAlpha() / 255.0);
    }

    public RGB alpha(float opacity) {
        return alpha((int)(255 * opacity));
    }

    public RGB alpha(int alpha) {
        return new RGB(red, green, blue, alpha);
    }

    void parse(String rgb) {
        StringBuilder sb = new StringBuilder(rgb);

        if (rgb.startsWith("#")) {
            sb.delete(0, 1);
        }

        if (sb.length() == 3) {
            sb.append(sb.toString());
        }

        if (sb.length() == 8) {
            alpha = Integer.parseInt(sb.substring(0,2), 16);
            sb.delete(0, 2);
        }

        if (sb.length() != 6) {
            throw new IllegalArgumentException("Unable to parse " + rgb + " as RGB");
        }

        red = Integer.parseInt(sb.substring(0,2), 16);
        green = Integer.parseInt(sb.substring(2,4), 16);
        blue = Integer.parseInt(sb.substring(4,6), 16);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + alpha;
        result = prime * result + blue;
        result = prime * result + green;
        result = prime * result + red;
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
        if (alpha != other.alpha)
            return false;
        if (blue != other.blue)
            return false;
        if (green != other.green)
            return false;
        if (red != other.red)
            return false;
        return true;
    }

    
}
