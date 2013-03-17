package org.jeo.agg;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.geom.CoordinatePath;
import org.jeo.geom.Geom;
import org.jeo.map.Layer;
import org.jeo.map.Map;
import org.jeo.map.RGB;
import org.jeo.map.Rule;
import org.jeo.map.RuleSet;
import org.jeo.map.Selector;
import org.jeo.map.Stylesheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class AggRenderer {

    static Logger LOG = LoggerFactory.getLogger(AggRenderer.class);

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

    Map map;
    long rp;
    long rb;

    VertexPathBuffer vpb;

    public AggRenderer() {
    }

    public void init(Map map) {
        this.map = map;
        vpb = new VertexPathBuffer();

        rb = createRenderingBuffer(map.getWidth(), map.getHeight());
        rp = createRenderingPipeline();

        setTransform(rp, map.scaleX(), -1*map.scaleY(), map.translateX(), map.translateY());

        Stylesheet style = map.getStyle();

        Rule rule = style.selectByName("Map").first();
        if (rule != null) {
            RGB bg = rule.color("background-color", null);
            setBackground(rb, color(bg));
        }
    }

    private native long createRenderingBuffer(int width, int height);

    private native long createRenderingPipeline();
    
    private native void setTransform(long rph, double scx, double scy, double tx, double ty);

    public void render() {
        // Overall algorithm:
        //
        // for each layer l
        //   - find all matching rules
        //   - flatten the rules
        //   - group rules by attachment / z-index
        //   - sort the rules and collapse where possible
        //   - render the features
        //   - composite the rendering buffer into previous

        for (Layer l : map.getLayers()) {
            List<RuleSet> rules = zgroup(flatten(match(l, map.getStyle())));

            //allocate the buffers
            for (RuleSet ruleSet : rules) {
                long buf = createRenderingBuffer(map.getWidth(), map.getHeight());
                render((Vector) l.getData(), ruleSet, buf);
                //render((Vector) l.getData(), ruleSet, rb);

                composite(rb, buf, ruleSet);
                disposeBuffer(buf);
            }
        }
    }

    public void dispose() {
        if (rp != 0) {
            disposePipeline(rp);
            rp = 0;
        }

        if (rb != 0) {
            disposeBuffer(rb);
            rb = 0;
        }
    }

    void render(Vector l, RuleSet ruleSet, long buf) {
        try {
            for (Feature f : l.read(map.getBounds())) {
                Rule r = ruleSet.match(f).collapse();
                if (r != null) {
                    draw(f, r, buf);
                }
            }
        } catch (IOException e) {
            LOG.error("Error querying layer " + l.getName(), e);
        }
    }

    void composite(long dstRb, long srcRb, RuleSet rules) {
        composite(dstRb, srcRb, "src");
    }

    private native void composite(long dstRb, long srcRb, String mode);

    private native void disposeBuffer(long rbh);

    private native void disposePipeline(long rph);

    RuleSet match(Layer layer, Stylesheet style) {
        return style.selectById(layer.getName(), true);
    }

    RuleSet flatten(RuleSet rules) {
        List<Rule> flat = new ArrayList<Rule>();
        for (Rule r : rules) {
            flat.addAll(r.flatten());
        }
        return new RuleSet(flat);
    }

    List<RuleSet> zgroup(RuleSet rules) {
        LinkedHashMap<String, List<Rule>> z = new LinkedHashMap<String, List<Rule>>();
        for (Rule r : rules) {
            String att = null;
            for (Iterator<Selector> it = r.getSelectors().iterator(); it.hasNext() && att == null;) {
                att = it.next().getAttachment();
            }

            List<Rule> list = z.get(att);
            if (list == null) {
                list = new ArrayList<Rule>();
                z.put(att, list);
            }
            list.add(r);
        }

        List<RuleSet> grouped = new ArrayList<RuleSet>();
        for (List<Rule> l : z.values()) {
            grouped.add(new RuleSet(l));
        }
        return grouped;
    }

    List<Rule> sortAndMerge(List<Rule> rules) {
        //TODO: merge
        //sort rules based on how specific they are
        Collections.sort(rules, new Comparator<Rule>() {
            @Override
            public int compare(Rule r1, Rule r2) {
                return -1*Integer.valueOf(specificity(r1)).compareTo(specificity(r2));
            }
        });
        return rules;
    }

    int specificity(Rule r) {
        int value = 0;
        for (Selector s : r.getSelectors()) {
            value = Math.max(value, specificity(s));
        }
        return value;
    }

    int specificity(Selector s) {
        //based on:
        // 
        int value = 0;
        if (s.getId() != null) {
            value += 100;
        }
        if (s.getAttachment() != null) {
            value += 10;
        }

        value += 10 * s.getClasses().size();

        if (s.getName() != null) {
            value += 1;
        }

        return value;
    }

    private native void setBackground(long rbh, float[] color);

    void draw(Feature f, Rule rule, long buf) {
        Geometry g = f.geometry();
        if (g == null) {
            return;
        }

        vpb.reset();
        vpb.fill( CoordinatePath.create(g, true, map.iscaleX()*0.5, map.iscaleY()*0.5));

        switch(Geom.Type.from(g)) {
        case LINESTRING:
        case MULTILINESTRING:
            drawLine(f, rule, buf);
            return;
        case POLYGON:
        case MULTIPOLYGON:
            drawPolygon(f, rule, buf);
            return;
        default:
            throw new UnsupportedOperationException();
        }
    }

    void drawLine(Feature f, Rule rule, long buf) {
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
        drawLine(rp, buf, vpb.buffer(), color(color), width, join, cap, dash, compOp);
    }

    private native void drawLine(long rph, long rbh, ByteBuffer path, float[] color, float width, 
        byte join, byte cap, double[] dash, String compOp);

    void drawPolygon(Feature f, Rule rule, long buf) {
        RGB polyFill = rule.color("polygon-fill", null);
        if (polyFill != null) {
            polyFill = polyFill.alpha(rule.number("polygon-opacity", 1f));
        }

        RGB lineColor = rule.color("line-color", null);
        if (lineColor != null) {
            lineColor = lineColor.alpha(rule.number("line-opacity", 1f));
        }

        float lineWidth = rule.number("line-width", 1f);
        String compOp = rule.string("comp-op", null);

        drawPolygon(rp, buf, vpb.buffer(), color(polyFill), color(lineColor), lineWidth, compOp);
    }

    private native void drawPolygon(long rph, long rbh, ByteBuffer path, float[] fillColor, 
        float[] lineColor, float lineWidth, String compOp);

    public void writePPM(String path) {
        writePPM(rb, path);
    }

    private native void writePPM(long handle, String path);

    public int[] data() {
        return data(rb);
    }

    private native int[] data(long handle);

    private float[] color(RGB rgb) {
        if (rgb == null) {
            return null;
        }
        return new float[]{rgb.getRed(), rgb.getGreen(), rgb.getBlue(), rgb.getAlpha()};
    }
}
