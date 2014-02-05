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
package org.jeo.svg;

import static org.jeo.map.CartoCSS.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jeo.feature.Feature;
import org.jeo.geom.CoordinatePath;
import org.jeo.geom.Envelopes;
import org.jeo.map.RGB;
import org.jeo.map.Rule;
import org.jeo.map.RuleList;
import org.jeo.map.View;
import org.jeo.map.render.BaseRenderer;
import org.jeo.map.render.Label;
import org.jeo.map.render.LabelIndex;
import org.jeo.map.render.Labeller;
import org.jeo.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Renderer producing Scalable Vector Graphics (SVG) output.
 */
public class SVGRenderer extends BaseRenderer implements Labeller {

    static final Logger LOG = LoggerFactory.getLogger(SVGRenderer.class);

    static final String URI = "http://www.w3.org/2000/svg";
    static final String VERSION = "1.2";
    static final String PROFILE = "tiny";
    static final String INDENT_AMOUNT_KEY = "{http://xml.apache.org/xslt}indent-amount";

    static enum TextAnchor { start,  middle, end };

    Properties outputProps;
    TransformerHandler tx;

    public SVGRenderer() {
        outputProps = new Properties();
        outputProps.put(OutputKeys.METHOD, "XML");
    }

    public SVGRenderer indent(int size) {
        if (size > 0) {
            outputProps.put(OutputKeys.INDENT, "yes");
            outputProps.put(INDENT_AMOUNT_KEY, String.valueOf(size));
            //outputProps.put(OutputKeys.INDENT, "yes");
        }
        return this;
    }

    protected TransformerHandler createTransformer(OutputStream output) throws IOException {
        //create the document seriaizer
        SAXTransformerFactory txFactory = 
            (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        
        TransformerHandler tx;
        try {
            tx = txFactory.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new IOException(e);
        }
        //tx.getTransformer().setOutputProperties(outputProps);
        //tx.getTransformer().setOutputProperty(OutputKeys.METHOD, "XML");
        tx.getTransformer().setOutputProperties(outputProps);
        tx.setResult(new StreamResult(output));

        return tx;
    }

    @Override
    protected void onStart() {
        try {
            this.tx = createTransformer(output);
            tx.startDocument();
            start("svg", atts("width", view.getWidth(), "height", view.getHeight(), 
                "zoomAndPan", "magnify", 
                "xmlns", URI, "version", VERSION, "baseProfile", PROFILE).get());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onFinish() {
        try {
            end("svg");
            tx.endDocument();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void drawBackground(RGB color) {
        start("rect", atts("width", "100%", "height", "100%", "fill", color.rgbhex(), 
            "fill-opacity", color.getOpacity()).get()).end("rect");
    }

    @Override
    protected void drawPoint(Feature f, Rule rule, Geometry p) {
        String shape = rule.string(f, MARKER_TYPE, "circle");
        float width = rule.number(f, MARKER_WIDTH, 10f);
        float height = rule.number(f, MARKER_HEIGHT, width);

        RGB fillColor = markFillColor(f, rule);
        RGB lineColor = markLineColor(f, rule);

        Geometry point = f.geometry();

        if (fillColor != null || lineColor != null) {
            CoordinatePath path = path(point);
            drawShape(path, shape, width, height, fillColor, lineColor);
        }

        String label = rule.eval(f, TEXT_NAME, String.class);
        if (label != null) {
            Coordinate a = toScreenTransform().apply(point.getCoordinate());
            Label l = new Label(label, rule, f, point);
            l.setAnchor(a);
            l.put(Text.class, text(f, rule, label));
            insertLabel(l);
        }
    }

    @Override
    protected void drawLine(Feature f, Rule rule, Geometry line) {
        // line color + width 
        Stroke stroke = stroke(f, rule, RGB.black);
        drawPath(path(line), null, stroke);
        //float gamma = rule.number(f, "line-gamma", 1f);
        //String gammaMethod = rule.string(f, "line-gamma-method", "power");

    }

    @Override
    protected void drawPolygon(Feature f, Rule rule, Geometry poly) {
        RGB polyFill = rule.color(f, POLYGON_FILL, null);
        if (polyFill != null) {
            polyFill = polyFill.alpha(rule.number(f, POLYGON_OPACITY, 1f));
        }

        Stroke s = stroke(f, rule, null);
        drawPath(path(poly), polyFill, s);
    }

    void drawShape(CoordinatePath path, String type, float width, float height, RGB fill, RGB stroke) {
        Shape shape = shape(type);

        type = shape.name().toLowerCase();

        AttributeBuilder ab = atts();
        if (fill != null) {
            ab.kv("fill", fill.rgbhex(), "fill-opacity", fill.getOpacity());
        }
        if (stroke != null) {
            ab.kv("stroke", stroke.rgbhex(), "stroke-opacity", stroke.getOpacity());
        }

        switch(shape) {
        case circle:
            ab.kv("r", width);
            break;
        case ellipse:
            ab.kv("rx", width, "ry", height);
            break;
        case rect:
            ab.kv("width", width, "height", height);
        }

        while(path.hasNext()) {
            Coordinate c = path.next();
            ab.kv("cx", c.x, "cy", c.y);

            start(type, ab.get()).end(type);
        }
    }

    void drawPath(CoordinatePath path, RGB fill, Stroke stroke) {

        AttributeBuilder ab = apply(stroke, atts("fill", fill != null ? fill.rgbhex() : "none")); 

        StringBuilder d = new StringBuilder();
        while(path.hasNext()) {
            Coordinate c = path.next();
            switch(path.getStep()) {
            case MOVE_TO:
                d.append("M ").append(c.x).append(" ").append(c.y).append(" ");
                break;
            case LINE_TO:
                d.append("L ").append(c.x).append(" ").append(c.y).append(" ");
                break;
            case CLOSE:
                d.append("Z ");
            case STOP:
            }
        }

        start("path", ab.kv("d", d.toString(), "focusable", "true").get()).end("path");
    }

    String toString(float[] array) {
        if (array.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]).append(",");
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    CoordinatePath path(Geometry g) {
        return CoordinatePath.create(g).generalize(view.iscaleX(),view.iscaleY())
            .transform(toScreenTransform());
    }

    RGB markFillColor(Object obj, Rule rule) {
        RGB fillColor = rule.color(obj, MARKER_FILL, null);
        if (fillColor == null) {
            return null;
        }

        fillColor = fillColor.alpha(rule.number(obj, MARKER_FILL_OPACITY, 1f));
        return fillColor;
    }

    RGB markLineColor(Object obj, Rule rule) {
        RGB lineColor = rule.color(obj, MARKER_LINE_COLOR, null);
        if (lineColor == null) {
            return null;
        }

        return lineColor.alpha(rule.number(obj, MARKER_LINE_OPACITY, 1f));
    }

    Shape shape(String str) {
        if (str != null) {
            try {
                return Shape.valueOf(str.toLowerCase());
            }
            catch(IllegalArgumentException e) {
                LOG.debug("unsupported shape: " + str + " falling back to 'circle'");
                return Shape.circle;
            }
        }
        return Shape.circle;
    }

    Join join(String str) {
        if (str != null) {
            try {
                return Join.valueOf(str.toLowerCase());
            }
            catch(IllegalArgumentException e) {
                LOG.debug("unsupported join: " + str + " falling back to 'miter'");
            }
        }
        return Join.miter;
    }

    Cap cap(String str) {
        if (str != null) {
            try {
                return Cap.valueOf(str.toLowerCase());
            }
            catch(IllegalArgumentException e) {
                LOG.debug("unsupported cap: " + str + " falling back to 'butt'");
            }
        }
        return Cap.butt;
    }

    Font.Weight fontWeight(String str) {
        if (str != null) {
            try {
                return Font.Weight.valueOf(str.toLowerCase());
            }
            catch(IllegalArgumentException e) {
                LOG.debug("unsupported font-weight: " + str + " falling back to 'normal'");
            }
        }
        return Font.Weight.normal;
    }

    Font.Style fontStyle(String str) {
        if (str != null) {
            try {
                return Font.Style.valueOf(str.toLowerCase());
            }
            catch(IllegalArgumentException e) {
                LOG.debug("unsupported font-weight: " + str + " falling back to 'normal'");
            }
        }
        return Font.Style.normal;
    }

    Text.Direction textDirection(String str) {
        if (str != null) {
            try {
                return Text.Direction.valueOf(str.toLowerCase());
            }
            catch(IllegalArgumentException e) {
                LOG.debug("unsupported font-weight: " + str + " falling back to 'ltr'");
            }
        }
        return Text.Direction.ltr;
    }

    Text.Anchor textAnchor(String str) {
        if (str != null) {
            try {
                return Text.Anchor.valueOf(str.toLowerCase());
            }
            catch(IllegalArgumentException e) {
                LOG.debug("unsupported font-weight: " + str + " falling back to 'start'");
            }
        }
        return Text.Anchor.start;
    }

    Unit unit(String str) {
        if (str != null) {
            try {
                return Unit.valueOf(str.toLowerCase());
            }
            catch(IllegalArgumentException e) {
                LOG.debug("unsupported unit: " + str + " falling back to 'pixel'");
            }
        }
        return Unit.pixel;
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


    SVGRenderer start(String name, Attributes atts) {
        try {
            tx.startElement(null, null, name, atts);
            return this;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    SVGRenderer text(String text) {
        try {
            tx.characters(text.toCharArray(), 0, text.length());
            return this;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    } 

    SVGRenderer end(String name) {
        try {
            tx.endElement(null, null, name);
            return this;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    AttributeBuilder atts(Object... kv) {
        return new AttributeBuilder().kv(kv);
    }

    AttributeBuilder apply(Stroke s, AttributeBuilder ab) {
        if (s != null) {
            ab.kv("stroke", s.color.rgbhex(), "stroke-width", s.width, "stroke-linejoin", s.join, 
                "stroke-linecap", s.cap);
            if (s.dash != null) {
                ab.kv("stroke-dasharray", toString(s.dash), "stroke-dashoffset", s.dashOffset);
            }
        }
        return ab;
    }

    static class AttributeBuilder {
        AttributesImpl atts;

        public AttributeBuilder() {
            atts = new AttributesImpl();
        }

        public AttributeBuilder kv(Object...kv) {
            if (kv.length % 2 != 0) {
                throw new IllegalArgumentException("non even number of key value pairs");
            }
            for (int i = 0; i < kv.length; i+=2) {
                atts.addAttribute(null, null, String.valueOf(kv[i]), null, String.valueOf(kv[i+1]));
            }
            return this;
        }

        public Attributes get() {
            return atts;
        }
    }

    Stroke stroke(Feature f, Rule rule, RGB defcolor) {
        Stroke s = new Stroke();

        RGB color = rule.color(f, LINE_COLOR, defcolor);
        if (color == null) {
            return null;
        }
       
        s.color = color.alpha(rule.number(f, LINE_OPACITY, 1f));
        s.width = rule.number(f, LINE_WIDTH, 1f);

        // line join
        s.join = join(rule.string(f, LINE_JOIN, "miter"));
        
        // line cap 
        s.cap = cap(rule.string(f, LINE_CAP, "butt"));

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
        s.dash = dash;
        s.dashOffset = rule.number(f, LINE_DASH_OFFSET, 0f);

        return s;
    }

    Text text(Feature f, Rule rule, String text) {
        Font font = new Font();
        font.family = rule.string(f, TEXT_FACE_NAME, "sans-serif");
        font.weight = fontWeight(rule.string(f, "text-weight", null));

        Pair<Float,Unit> size = size(rule.string(f, TEXT_SIZE, null));
        font.size = size.first();
        font.unit = size.second();

        Text t = new Text(text);
        t.font = font;
        t.direction = textDirection(rule.string(f, "text-direction", null));
        t.anchor = textAnchor(rule.string(f, TEXT_ALIGN, null));
        return t;
    }

    static Pattern SIZE_RE = Pattern.compile("([0-9]+)(\\w+)?");

    Pair<Float,Unit> size(String str) {
        if (str != null) {
            Matcher m = SIZE_RE.matcher(str);
            if (m.matches()) {
                Float value = Font.DEFAULT_SIZE;
                try {
                    value = Float.parseFloat(m.group(1));
                }
                catch(NumberFormatException e) {
                    LOG.debug("illegal number value: " +m.group(1)+ ", falling back to:" + value);
                }

                return Pair.of(value, unit(m.group(2)));
            }
        }
        return Pair.of(Font.DEFAULT_SIZE, Unit.pixel);
    }

    @Override
    protected Labeller createLabeller() {
        return this;
    }

    @Override
    public boolean layout(Label label, LabelIndex labels) {

        Text text = label.get(Text.class, Text.class);
        Feature f = label.getFeature();

        Rule rule = label.getRule();

        Coordinate anchor = label.anchor();

        // apply offsets
        anchor.x += rule.number(f, TEXT_DX, 0f);
        anchor.y += rule.number(f, TEXT_DY, 0f);

        //compute bounds of this label
        float height = text.font.size;
        float width = (float) (text.value.length() * height * 0.5);

        Envelope bbox = new Envelope(anchor.x, anchor.x + width, anchor.y, anchor.y + height);

        switch(text.anchor) {
        case middle:
            bbox = Envelopes.translate(bbox, -width/2f, 0);
            break;
        case end:
            bbox = Envelopes.translate(bbox, -width, 0);
            break;
        }

        label.setBounds(bbox);

        if (!view.window().contains(bbox)) {
            return false;
        }

        return labels.insert(label);
    }

    @Override
    public void render(Label label) {
        if (debugLabels()) {
            Envelope box = label.bounds();
            start("rect", atts("fill", "none", "stroke", "black", "x", box.getMinX(), 
                "y", box.getMinY() - box.getHeight(), "width", box.getWidth(), 
                "height", box.getHeight()).get()).end("rect");
        }

        Coordinate a = label.anchor();
        Text text = label.get(Text.class, Text.class);
        Font font = text.font;

        Attributes atts = atts("x", a.x, "y", a.y, "font-family", font.family,
                "font-size", font.size, "text-anchor", text.anchor, "direction", text.direction).get();
        start("text", atts).text(label.getText()).end("text");
    }

    @Override
    public void close() {
    }

    boolean debugLabels() {
        RuleList rules = view.getMap().getStyle().getRules().selectByName("Map", false);
        if (rules.isEmpty()) {
            return false;
        }

        return rules.collapse().bool(null, "debug-labels", false);
    }
}
