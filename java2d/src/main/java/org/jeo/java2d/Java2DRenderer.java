package org.jeo.java2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.List;

import org.jeo.data.Query;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.geom.CoordinatePath;
import org.jeo.geom.Geom;
import org.jeo.map.Layer;
import org.jeo.map.Map;
import org.jeo.map.RGB;
import org.jeo.map.Rule;
import org.jeo.map.Stylesheet;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class Java2DRenderer {

    Graphics2D context;
    Map map;
    AffineTransform toScreenTx;

    public Java2DRenderer(Graphics2D context) {
        this.context = context;
    }

    public void init(Map map) {
        this.map = map;

        //initialize the graphics context
        context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //calculate world to screen transform
        toScreenTx = createToScreenTransform(map);
    }

    public AffineTransform getToScreenTransform() {
        return toScreenTx;
    }

    AffineTransform createToScreenTransform(Map map) {
        return new AffineTransform(
            map.scaleX(), 0d, 0d, -map.scaleY(), map.translateX(), map.translateY());
    }

    public void render() throws IOException {
        Stylesheet style = map.getStyle();

        //draw background
        context.setColor(color((RGB)style.get("background-color", RGB.white)));
        context.fillRect(0, 0, map.getWidth(), map.getHeight());

        for (Layer layer : map.getLayers()) {
            render(layer);
        }
    }

    public void render(Layer layer) throws IOException {
        if (!(layer.getData() instanceof Vector)) {
            throw new UnsupportedOperationException("Only vector layers supported");
        }

        Vector data = (Vector) layer.getData();
        for (Feature f : data.cursor(new Query().bounds(map.getBounds()))) {
            render(f, layer);
        }
    }

    public void render(Feature f, Layer layer) {
        Geometry g = f.geometry();
        if (g == null) {
            return;
        }

        Rule rule = map.getStyle().selectById(layer.getName(), true).first();

        switch(Geom.Type.from(g)) {
        case POINT:
        case MULTIPOINT:
            //drawPoint(g);
            break;
        case LINESTRING:
        case MULTILINESTRING:
            //drawLineString(g);
            break;
        case POLYGON:
        case MULTIPOLYGON:
            drawPolygon(g, f, rule);
            break;
        case GEOMETRYCOLLECTION:
            //drawGeometryCollection(g);
        default:
            throw new IllegalArgumentException();
        }
    }

    void drawPolygon(Geometry g, Feature f, Rule rule) {
        LiteShape shp = new LiteShape(g, toScreenTx, false);

        //render the fill
        context.setColor(color(rule, "polygon-fill", "polygon-opacity", RGB.gray));
        context.fill(shp);

        //render the outline
        context.setColor(color(rule, "line-color", "line-opacity", RGB.black));
        context.setStroke(stroke(rule));
        context.draw(shp);
    }

    public void render(Geometry g) {
        if (g == null) {
            return;
        }

        context.draw(new LiteShape(g, toScreenTx, false));
    }

    public Color color(RGB rgb) {
        return new Color(rgb.getRed(), rgb.getGreen(), rgb.getGreen(), rgb.getAlpha());
    }

    Color color(Rule rule, String colorKey, String opacityKey, RGB def) {
        RGB rgb = (RGB) rule.get(colorKey, def);
        Number opacity = (Number) rule.get(opacityKey, null);

        if (opacity != null) {
            rgb = rgb.alpha(opacity.floatValue());
        }

        return color(rgb);
    }

    Stroke stroke(Rule rule) {
        float width = ((Number)rule.get("line-width", 1.0)).floatValue();
        return new BasicStroke(width);
    }
}
