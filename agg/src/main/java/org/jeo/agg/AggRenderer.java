package org.jeo.agg;

import org.jeo.feature.Feature;
import org.jeo.map.Map;
import org.jeo.map.RGB;
import org.jeo.map.Rule;
import org.jeo.map.Stylesheet;

import com.vividsolutions.jts.geom.Geometry;

public class AggRenderer {

    static class LineCap {
        static byte BUTT = 0;
        static byte SQUARE = 1;
        static byte ROUND = 2;

        static byte value(String name) {
            if ("butt".equalsIgnoreCase(name)) {
                return BUTT;
            }
            if ("square".equalsIgnoreCase(name)) {
                return SQUARE;
            }
            if ("round".equalsIgnoreCase(name)) {
                return ROUND;
            }

            return -1;
        }
    }

    static class LineJoin {
        static byte MITER = 0;
        static byte ROUND = 2;
        static byte BEVEL = 3;
    }

    long rp;

    public void init(Map map) {
        rp = createRenderingPipeline(map.getWidth(), map.getHeight());

        setTransform(rp, map.scaleX(), -1*map.scaleY(), map.translateX(), map.translateY());

        Stylesheet style = map.getStyle();
        RGB bg = (RGB) style.get("background-color", null);
        if (bg != null) {
            setBackground(rp, color(bg));
        }
    }

    private native long createRenderingPipeline(int width, int height);

    private native void setTransform(long handle, double scx, double scy, double tx, double ty);

    private native void setBackground(long handle, float[] color);

    void drawLine(Geometry g, Feature f, Rule rule) {
        RGB color = (RGB) rule.get("line-color", RGB.black);
        float width = rule.number("line-width", 1f);

        byte join = LineCap.value(rule.string("line-join", "miter"));
        join = join != -1 ? join : LineJoin.MITER;

        byte cap = LineCap.value(rule.string("line-cap", "butt"));
        cap = cap != -1 ? cap : LineCap.BUTT;

        double[] dash = rule.numbers("line-dasharray", null); 
        if (dash != null && dash.length % 2 != 0) {
            throw new IllegalArgumentException("line-dasharray pattern must be even length");
        }

        String compOp = rule.string("comp-op", null);
        drawLine(rp, new VertexSource(g), color(color), width, join, cap, dash, compOp);
    }

    private native void drawLine(long handle, VertexSource g, float[] color, float width, byte join,
        byte cap, double[] dash, String compOp);

    void drawPolygon(Geometry g, Feature f, Rule rule) {
        RGB polyFill = (RGB) rule.get("polygon-fill", RGB.gray);
        polyFill = polyFill.alpha(rule.number("polygon-opacity", 1f));
        
        RGB lineColor = (RGB) rule.get("line-color", RGB.black);
        lineColor = lineColor.alpha(rule.number("line-opacity", 1f));

        float lineWidth = ((Number)rule.get("line-width", 1f)).floatValue();
        String compOp = rule.string("comp-op", null);

        drawPolygon(rp, new VertexSource(g), color(polyFill), color(lineColor), lineWidth, compOp);
    }

    private native void drawPolygon(long handle, VertexSource g, float[] fillColor, 
            float[] lineColor, float lineWidth, String compOp);

    public void writePPM(String path) {
        writePPM(rp, path);
    }

    private native void writePPM(long handle, String path);

    public int[] data() {
        return data(rp);
    }

    private native int[] data(long handle);

    private float[] color(RGB rgb) {
        return new float[]{rgb.getRed(), rgb.getGreen(), rgb.getBlue(), rgb.getAlpha()};
    }

}
