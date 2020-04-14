package io.jeo.geotools.render;

import static io.jeo.map.CartoCSS.*;
import static io.jeo.geotools.render.GTRendererFactory.*;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.LiteShape2;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.renderer.label.LabelCacheImpl;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.StyledShapePainter;
import org.geotools.renderer.style.DynamicSymbolFactoryFinder;
import org.geotools.renderer.style.LineStyle2D;
import org.geotools.renderer.style.MarkFactory;
import org.geotools.renderer.style.MarkStyle2D;
import org.geotools.renderer.style.PolygonStyle2D;
import org.geotools.renderer.style.WellKnownMarkFactory;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import io.jeo.vector.Feature;
import io.jeo.filter.Expression;
import io.jeo.filter.Literal;
import io.jeo.geotools.GT;
import io.jeo.map.Layer;
import io.jeo.map.Map;
import io.jeo.map.RGB;
import io.jeo.map.Rule;
import io.jeo.map.View;
import io.jeo.render.BaseRenderer;
import io.jeo.util.Rect;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;
import org.opengis.style.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class GTRenderer extends BaseRenderer {

    static Logger LOG = LoggerFactory.getLogger(GTRenderer.class);

    /**
     * factory for geotools style objects
     */
    static StyleBuilder STYLES = new StyleBuilder();

    /**
     * factory for geotools expressions
     */
    static FilterFactory FILTERS = CommonFactoryFinder.getFilterFactory();

    static org.opengis.filter.expression.Expression NORMAL = FILTERS.literal("normal");

    /**
     * image to render to
     */
    BufferedImage img;

    /**
     * graphics context
     */
    Graphics2D g;

    /**
     * affine world to screen transform
     */
    AffineTransform2D tx;

    /**
     * the styler
     */
    StyledShapePainter painter;

    /**
     * labels
     */
    LabelCache labelCache;

    /**
     * name of current layer for labeller
     */
    String currLayer;

    public GTRenderer(BufferedImage img) {
        this.img = img;
    }

    @Override
    protected boolean canRenderVectors() {
        return true;
    }

    @Override
    protected boolean canRenderRasters() {
        return true;
    }

    @Override
    protected boolean canRenderTiles() {
        return false;
    }

    @Override
    public void init(View view, java.util.Map<?, Object> opts) {
        super.init(view, opts);

        g = img.createGraphics();

        // create affine transform
        tx = new AffineTransform2D(
            view.scaleX(), 0d, 0d, -view.scaleY(), view.translateX(), view.translateY());

        Map map = view.getMap();
        Float gamma = map.getStyle().getRules().selectByName("map", false, false).collapse()
            .number(map, "gamma", 1f);
        if (gamma > 0) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // TODO: clipping?

        // initialize the label cache
        labelCache = new LabelCacheImpl();

        painter = new StyledShapePainter(labelCache);
    }

    @Override
    public void close() {
        if (g != null) {
            g.dispose();
            g = null;
        }
    }

    @Override
    protected void onStart() throws IOException {
        super.onStart();
        labelCache.start();
    }

    @Override
    protected void onLayerStart(Layer layer) throws IOException {
        super.onLayerStart(layer);

        currLayer = layer.getName();
        labelCache.startLayer(currLayer);
    }

    @Override
    protected void onLayerFinish(Layer layer) {
        super.onLayerFinish(layer);
        labelCache.endLayer(layer.getName(), g, rect(view.window()));
    }

    @Override
    protected void onFinish() throws IOException {
        labelCache.end(g, rect(view.window()));

        if (output != null) {
            ImageIO.write(img, IMAGE_FORMAT.get(opts).toUpperCase(Locale.ROOT), output);
        }
    }

    Rectangle rect(Rect win) {
        return new Rectangle(win.left, win.top, win.width(), win.height());
    }

    @Override
    protected void drawBackground(RGB color) {
        
    }

    @Override
    protected void drawPoint(Feature f, Rule rule, Geometry point) {
        /*
        marker-file
        marker-placement
        marker-allow-overlap
        marker-ignore-placement
        marker-spacing
        marker-max-error
        marker-transform
        marker-clip
        marker-smooth
        marker-geometry-transform
        */

        MarkStyle2D style = new MarkStyle2D();

        float width = rule.number(f, MARKER_WIDTH, -1f);
        float height = rule.number(f, MARKER_HEIGHT, -1f);
        style.setSize(!eq(-1f,width) ? width : !eq(-1f,height) ? height : 10d);

        String compOp = rule.string(f, MARKER_COMP_OP, "src-over");
        float opacity = rule.number(f, MARKER_OPACITY, -1f);

        RGB fillColor = rule.color(f, MARKER_FILL, null);
        if (fillColor != null) {
            float o = eq(-1f,opacity) ? rule.number(f, MARKER_FILL_OPACITY, 1f) : opacity;
            fillColor = fillColor.alpha(opacity);
            style.setFill(color(fillColor));
            if (compOp != null) {
                style.setFillComposite(comp(compOp, o));
            }
        }

        RGB lineColor = rule.color(f, MARKER_LINE_COLOR, null);
        if (lineColor != null) {
            float o = eq(-1f,opacity) ? rule.number(f, MARKER_LINE_OPACITY, 1f) : opacity;
            lineColor = lineColor.alpha(opacity);

            style.setContour(color(lineColor));
            if (compOp != null) {
                style.setContourComposite(comp(compOp, o));
            }

            style.setStroke(new BasicStroke(rule.number(f, MARKER_LINE_WIDTH, 1f)));
        }

        String type = rule.string(f, MARKER_TYPE, "circle");
        style.setShape(marker(type, f));
        LiteShape2 shape = shape(point);
        painter.paint(g, shape, style, 1);

        doLabel(f, rule, point);
    }

    @Override
    protected void drawLine(Feature f, Rule rule, Geometry line) {
        /*
        line-gamma
        line-gamma-method
        line-miterlimit
        line-clip
        line-smooth
        line-offset
        line-rasterizer
        line-geometry-transform
        */
        LineStyle2D style = new LineStyle2D();
        style.setStroke(lineStroke(f, rule));
        style.setContour(linePaint(f, rule));
        style.setContourComposite(comp(rule.string(f, LINE_COMP_OP, "src-over"), 
            rule.number(f, LINE_OPACITY, 1f)));

        // the line
        LiteShape2 shape = shape(line);  
        painter.paint(g, shape, style, 1);

        // labels
        doLabel(f, rule, line);
    }

    @Override
    protected void drawPolygon(Feature f, Rule rule, Geometry poly) {
        /*
        polygon-gamma
        polygon-gamma-method
        polygon-clip
        polygon-smooth
        polygon-geometry-transform

        line-gamma
        line-gamma-method
        line-miterlimit
        line-clip
        line-smooth
        line-offset
        line-rasterizer
        line-geometry-transform
        */
        PolygonStyle2D style = new PolygonStyle2D();

        String polyCompOp = rule.string(f, POLYGON_COMP_OP, "src-over");
        float opacity = rule.number(f, POLYGON_OPACITY, -1f);

        RGB fillColor = rule.color(f, POLYGON_FILL, null);
        if (fillColor != null) {
            float o = eq(-1f,opacity) ? rule.number(f, POLYGON_OPACITY, 1f) : opacity;
            fillColor = fillColor.alpha(opacity);
            style.setFill(color(fillColor));
            style.setFillComposite(comp(polyCompOp, o));
        }

        String lineCompOp = rule.string(f, LINE_COMP_OP, "src-over");
        RGB lineColor = rule.color(f, LINE_COLOR, null);
        if (lineColor != null) {
            float o = eq(-1f,opacity) ? rule.number(f, LINE_OPACITY, 1f) : opacity;
            lineColor = lineColor.alpha(opacity);

            style.setContour(color(lineColor));
            style.setContourComposite(comp(lineCompOp, o));

            style.setStroke(lineStroke(f, rule));
        }

        LiteShape2 shape = shape(poly);
        painter.paint(g, shape, style, 1);

        doLabel(f, rule, poly);
    }

    void doLabel(Feature f, Rule rule, Geometry line) {
        TextSymbolizer text = text(f, rule);
        if (text != null) {
            labelCache.put(
                currLayer, text, GT.feature(f), shape(line), NumberRange.create(0d, 1d));
        }
    }

    TextSymbolizer text(Feature f, Rule rule) {
        /*
         text-ratio
         text-wrap-width
         text-wrap-before
         text-wrap-character
         text-spacing
         text-character-spacing
         text-line-spacing
         text-label-position-tolerance
         text-max-char-angle-delta

         text-avoid-edges
         text-min-distance
         text-min-padding
         text-min-path-length
         text-allow-overlap
         text-placement
         text-placement-type
         text-placements
         text-transform

         text-align
         text-clip
         text-comp-op
         */
        Expression label = expr(rule, TEXT_NAME, null);
        if (label != null) {
            TextSymbolizer text = STYLES.createTextSymbolizer();
            text.setLabel(GT.expr(label));

            // font
            text.setFont(font(f, rule));

            // fill
            RGB fill = rule.color(f, TEXT_FILL, RGB.black);
            STYLES.createFill(color(fill), rule.number(f,TEXT_OPACITY,1f));

            // halo
            float haloRadius = rule.number(f, TEXT_HALO_RADIUS, 0f);
            if (haloRadius > 0) {
                RGB haloFill = rule.color(f,TEXT_HALO_FILL,RGB.white);
                text.setHalo(STYLES.createHalo(color(haloFill), haloFill.getOpacity(), haloRadius));
            }

            // placement
            LabelPlacement place = labelPlacement(f, rule);
            if (place != null) {
                text.setLabelPlacement(place);
            }

            return text;
        }
        return null;
    }

    LabelPlacement labelPlacement(Feature f, Rule rule) {
        String place = rule.string(f, TEXT_PLACEMENT, "point");

        if ("point".equalsIgnoreCase(place)) {
            double dx = rule.number(f, TEXT_DX, 0d);
            double dy = rule.number(f, TEXT_DY, 0d);
            double r = rule.number(f, TEXT_ORIENTATION, 0d);
            double ax = halign(f, rule);
            double ay = valign(f, rule);

            return STYLES.createPointPlacement(ax, ay, dx, dy, r);
        }
        else if ("line".equals(place)) {
            return STYLES.createLinePlacement(0);
        }
        else {
            LOG.debug("unsupported label placement: " + place);
        }

        return null;
    }

    double halign(Feature f, Rule rule) {
        //left middle right auto
        String val = rule.string(f, TEXT_HORIZONTAL_ALIGNMENT, "middle");
        if ("middle".equalsIgnoreCase(val) || "auto".equalsIgnoreCase(val)) {
            return 0.5;
        }
        else if ("left".equalsIgnoreCase(val)) {
            return 0;
        }
        else if ("right".equalsIgnoreCase(val)) {
            return 1;
        }
        else {
            LOG.debug(String.format(Locale.ROOT, "unsupported value for %s: %s", TEXT_HORIZONTAL_ALIGNMENT, val));
            return 0.5;
        }
    }

    double valign(Feature f, Rule rule) {
        //top middle bottom auto
        String val = rule.string(f, TEXT_VERTICAL_ALIGNMENT, "middle");
        if ("middle".equalsIgnoreCase(val) || "auto".equalsIgnoreCase(val)) {
            return 0.5;
        }
        else if ("top".equalsIgnoreCase(val)) {
            return 0;
        }
        else if ("bottom".equalsIgnoreCase(val)) {
            return 1;
        }
        else {
            LOG.debug(String.format(Locale.ROOT, "unsupported value for %s: %s", TEXT_VERTICAL_ALIGNMENT, val));
            return 0.5;
        }
    }

    Font font(Feature f, Rule rule) {
        Expression family = expr(rule, TEXT_FACE_NAME, new Literal(java.awt.Font.SANS_SERIF));
        Expression size = expr(rule, TEXT_SIZE, new Literal(10));

        return STYLES.createFont(GT.expr(family), NORMAL, NORMAL, GT.expr(size)); 
    }

    Paint linePaint(Feature f, Rule rule) {
        RGB color = rule.color(f, LINE_COLOR, RGB.black);
        return color(color);
    }

    Stroke lineStroke(Feature f, Rule rule) {
        // line color + width 
        
        float width = rule.number(f, LINE_WIDTH, 1f);

        // line join
        int join = join(rule.string(f, LINE_JOIN, "miter"));
        
        // line cap 
        int cap = cap(rule.string(f, LINE_CAP, "butt"));

        // line dash
        float[] dash = dash(rule.numbers(f, LINE_DASHARRAY, (Float[])null));
        if (dash != null && dash.length % 2 != 0) {
            LOG.debug("dash specified odd number of entries");

            float[] tmp;
            if (dash.length > 2) {
                // strip off last
                tmp = new float[dash.length-1];
                System.arraycopy(dash, 0, tmp, 0, tmp.length);
            }
            else {
                // pad it
                tmp = new float[dash.length*2];
                System.arraycopy(dash, 0, tmp, 0, dash.length);
                System.arraycopy(dash, 0, tmp, dash.length, dash.length);
            }
        }

        float dashOffset = rule.number(f, LINE_DASH_OFFSET, 0f);
        
        //float gamma = rule.number(f, "line-gamma", 1f);
        //String gammaMethod = rule.string(f, "line-gamma-method", "power");

        //String compOp = rule.string(f, LINE_COMP_OP, null);

        return dash != null ? new BasicStroke(width, cap, join, 1, dash, dashOffset) : 
            new BasicStroke(width, cap, join);
    }

    Color color(RGB rgb) {
        return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
    }

    int cap(String cap) {
        String c = cap.toLowerCase(Locale.ROOT);
        return "round".equals(c) ? BasicStroke.CAP_ROUND : 
            "square".equals(c) ? BasicStroke.CAP_SQUARE : BasicStroke.CAP_BUTT;
    }

    int join(String join) {
        String j = join.toLowerCase(Locale.ROOT);
        return "round".equals(j) ? BasicStroke.JOIN_ROUND : 
            "bevel".equals(j) ? BasicStroke.JOIN_BEVEL : BasicStroke.JOIN_MITER;
    }
    
    float[] dash(Float[] dash) {
        if (dash == null) {
            return null;
        }

        float[] prim = new float[dash.length];
        for (int i = 0; i < prim.length; i++) {
            prim[i] = dash[i].floatValue();
        }
        return prim;
    }

    Shape marker(String type, Feature f) {
        SimpleFeature sf = GT.feature(f);
        for (Iterator<MarkFactory> it = DynamicSymbolFactoryFinder.getMarkFactories(); it.hasNext(); ) {
            MarkFactory mf = it.next();
            try {
                Shape marker = mf.getShape(g, FILTERS.literal(type), sf);
                if (marker != null) {
                    return marker;
                }
            } catch (Exception e) {
                LOG.trace("error looking up mark", e);
            }
        }

        LOG.debug(String.format(Locale.ROOT, "unable to load mark '%s'", type));
        try {
            return new WellKnownMarkFactory().getShape(g, FILTERS.literal(type), sf);
        } catch (Exception e) {
            // shouldn't happen (famous last words)
            throw new RuntimeException(e);
        }
    }

    static HashMap<String,Integer> COMP_OPS = new HashMap<String, Integer>();
    static {
        COMP_OPS.put("clear", AlphaComposite.CLEAR);
        COMP_OPS.put("src", AlphaComposite.SRC);
        COMP_OPS.put("dst", AlphaComposite.DST);
        COMP_OPS.put("src-over", AlphaComposite.SRC_OVER);
        COMP_OPS.put("dst-over", AlphaComposite.DST_OVER);
        COMP_OPS.put("src-in", AlphaComposite.SRC_IN);
        COMP_OPS.put("dst-in", AlphaComposite.DST_IN);
        COMP_OPS.put("src-out", AlphaComposite.SRC_OUT);
        COMP_OPS.put("dst-out", AlphaComposite.DST_OUT);
        COMP_OPS.put("src-atop", AlphaComposite.SRC_ATOP);
        COMP_OPS.put("dst-atop", AlphaComposite.DST_ATOP);
        COMP_OPS.put("xor", AlphaComposite.XOR);
    }

    Composite comp(String compOp, float alpha) {
        String op = compOp.toLowerCase(Locale.ROOT);
        if (!COMP_OPS.containsKey(op)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                "unsupported composition: %s, allowable values are %s", op, COMP_OPS.keySet())); 
        }

        return AlphaComposite.getInstance(COMP_OPS.get(op), alpha);
    }

    LiteShape2 shape(Geometry g) {
        try {
            return new LiteShape2(g, tx, null, true, Math.max(view.iscaleX(), view.iscaleY()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Expression expr(Rule rule, String key, Expression def) {
        Expression e = null;
        Object obj = rule.get(key);
        if (obj != null) {
            if (obj instanceof Expression) {
                e = (Expression) obj;
            }
            else {
                LOG.debug("object not an expression: " + obj);
            }
        }
        return e != null ? e : def;
    }

    boolean eq(float f1, float f2) {
        return Math.abs(f1 - f1) < 0.00000001;
    }

    ByteBuffer toHeap(ByteBuffer buf) {
        if (buf.isDirect()) {
            ByteBuffer tmp = ByteBuffer.allocate(buf.capacity());
            tmp.order(buf.order());
            tmp.put(buf);
            tmp.flip();
            buf = tmp;
        }
        return buf;
    }
    @Override
    protected void drawRasterGray(ByteBuffer gray, Rect pos, Rule rule) throws IOException {
        gray = toHeap(gray);
        WritableRaster raster = Raster.createBandedRaster(new DataBufferByte(gray.array(), gray.capacity()),
            pos.width(), pos.height(), pos.width(), new int[]{0}, new int[]{0},null);

        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false,
            Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        BufferedImage img = new BufferedImage(cm, raster, false, null);
        g.drawRenderedImage(img, transform(pos));
    }

    @Override
    protected void drawRasterRGBA(ByteBuffer rgba, Rect pos, Rule rule) throws IOException {
        rgba = toHeap(rgba);
        WritableRaster raster = Raster.createInterleavedRaster(
            new DataBufferByte(rgba.array(), pos.area()), pos.width(), pos.height(),
            pos.width() * 4, 4, new int[]{0,1,2,3}, null) ;
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false,
                Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        BufferedImage img = new BufferedImage(cm, raster, false, null);
        g.drawRenderedImage(img, transform(pos));
    }

    AffineTransform transform(Rect pos) {
        AffineTransform tx = new AffineTransform();
        tx.translate(pos.left, pos.top);
        return tx;
    }
}
