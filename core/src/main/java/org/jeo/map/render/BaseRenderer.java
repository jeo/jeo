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
package org.jeo.map.render;

import static org.jeo.map.CartoCSS.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.jeo.data.*;
import org.jeo.feature.Feature;
import org.jeo.filter.Filter;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.map.*;
import org.jeo.proj.Proj;
import org.jeo.raster.*;
import org.jeo.util.Function;
import org.jeo.util.Rect;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Base class for renderers.
 *
 */
public abstract class BaseRenderer implements Renderer {

    static final Logger LOG = LoggerFactory.getLogger(BaseRenderer.class);

    protected View view;
    protected java.util.Map<?, Object> opts;

    protected LabelIndex labels = new LabelIndex();
    protected Labeller labeller;

    protected OutputStream output;

    public void init(View view, java.util.Map<?,Object> opts) {
        this.view = view;
        this.opts = opts;

        this.labeller = createLabeller();
    }

    /**
     * Creates a new labeller instance.
     * <p>
     * By default this method returns {@link org.jeo.map.render.Labeller#NULL}.
     * </p>
     */
    protected Labeller createLabeller() {
        return Labeller.NULL;
    }

    public void render(OutputStream output) throws IOException {
        this.output = output;

        LOG.debug("Rendering map at " + view.getBounds());
        onStart();

        // background
        renderBackground();
        for (Layer l : view.getMap().getLayers()) {
            if (!l.isVisible()) {
                continue;
            }

            onLayerStart(l);

            Dataset data = l.getData();
            Filter<Feature> filter = l.getFilter();

            RuleList rules =
                view.getMap().getStyle().getRules().selectById(l.getName(), true).flatten();

            for (RuleList ruleList : rules.zgroup()) {
                if (data instanceof VectorDataset) {
                    render((VectorDataset)data, ruleList, filter);
                }
                else if (data instanceof RasterDataset) {
                    render((RasterDataset)data, ruleList);
                }
                else if (data instanceof TileDataset) {
                    render((TileDataset)data, rules);
                }
            }

            onLayerFinish(l);
        }

        //labels
        renderLabels();

        LOG.debug("Rendering complete");
        onFinish();
    }

    void renderBackground() throws IOException {
        RuleList rules = view.getMap().getStyle().getRules().selectByName("Map", false, false);
        if (rules.isEmpty()) {
            //nothing to do
            return;
        }

        Map map = view.getMap();
        Rule rule = rules.collapse();
        RGB bgColor = rule.color(map, BACKGROUND_COLOR, null);
        if (bgColor != null) {
            bgColor = bgColor.alpha(rule.number(map, OPACITY, 1f));
            drawBackground(bgColor);
        }
    }

    protected Envelope reproject(Query q, Envelope bbox, Dataset data) throws IOException {
        CoordinateReferenceSystem crs = data.crs();

        // reproject
        if (crs != null) {
            if (view.getCRS() != null && !Proj.equal(view.getCRS(), data.crs())) {
                if (q != null) {
                    q.reproject(view.getCRS());
                }
                bbox = Proj.reproject(bbox, view.getCRS(), crs);
            }
        }
        else {
            LOG.debug(
                "Layer "+data.getName()+" specifies no projection, assuming map projection");
        }

        return bbox;
    }

    void render(VectorDataset data, RuleList rules, Filter<Feature> filter) throws IOException {
        if (!canRenderVectors()) {
            throw new UnsupportedOperationException("renderer does not render vector data");
        }

        // build up the data query
        Query q = new Query();

        // bounds, we may have to reproject it
        Envelope bbox = reproject(q, view.getBounds(), data);
        
        q.bounds(bbox);
        if (filter != null) {
            q.filter(filter);
        }

        for (Feature f : data.cursor(q)) {
            RuleList rs = rules.match(f);
            if (rs.isEmpty()) {
                continue;
            }

            Rule r = rules.match(f).collapse();
            if (r != null) {
                draw(f, r);
            }
        }
    }

    void render(RasterDataset data, RuleList rules) throws IOException {
        if (!canRenderRasters()) {
            throw new UnsupportedOperationException("renderer does not render raster data");
        }

        // calculate bounding intersection of view and dataset
        Envelope bbox = data.bounds().intersection(view.getBounds());
        if (bbox.isNull()) {
            // nothing to do
            return;
        }

        // calculate position of raster on screen
        Rect pos = view.mapToWindow(bbox);

        // build up the query
        RasterQuery q = new RasterQuery().bounds(bbox).size(pos.size());


        Rule rule = rules.collapse();

        // band selection
        List<Band> bands = data.bands();
        if (bands.size() == 1) {
            // single band case
            q.bands(0);
        }
        else {
            // multi band case
            Integer[] bandmap = rule.numbers(null, "raster-bands", (Integer[])null);
            if (bandmap == null) {
                if (bands.size() < 3) {
                    throw new IllegalStateException("unable to map bands " + bands + " to RGB");
                }

                // band map not explicitly specified, try to infer it
                int r = -1, b = -1, g = -1;
                for (int i = 0; i < bands.size(); i++) {
                    Band band = bands.get(i);
                    if (r == -1 && band.color() == Band.Color.RED) {
                        r = i;
                    }
                    if (g == -1 && band.color() == Band.Color.GREEN) {
                        g = i;
                    }
                    if (b == -1 && band.color() == Band.Color.BLUE) {
                        b = i;
                    }
                }

                if (r == -1 || g == -1 || b == -1) {
                    //just take the bands as is in order
                    q.bands(0,1,2);
                }
                else {
                    q.bands(r,g,b);
                }
            }
            else {
                q.bands(bandmap);
            }
        }

        // buffer data type
        if (q.getBands().length > 1) {
            // pack the data into int pixels
            q.datatype(DataType.INT);
        }
        else {
            // use native data type
        }

        // get the raw data
        ByteBuffer raw = data.read(q);

        if (q.getBands().length == 1) {
            Band band = data.bands().get(q.getBands()[0]);

            final Function<Double,RGB> colormap;
            if (rule.has("raster-colorizer-stops")) {
                // map using colorizer
                final Colorizer colorizer = Colorizer.decode(rule);
                colormap = new Function<Double, RGB>() {
                    @Override
                    public RGB apply(Double value) {
                        return colorizer.map(value);
                    }
                };
            }
            else {
                // interpolate to gray
                Stats stats = band.stats();
                final double min = stats.min();
                final double span = stats.max() - stats.min();
                colormap = new Function<Double, RGB>() {
                    @Override
                    public RGB apply(Double value) {
                        if (value == null) {
                            // TODO: replace with an actual nodata color
                            return RGB.black;
                        }

                        byte gray = (byte) (255 * ((value-min) / span));
                        return new RGB(gray, gray, gray);
                    }
                };
            }

            drawRasterRGBA(convertToRGBA(raw, colormap, band, rule), pos, rule);
        }
        else {
            // apply opacity and draw directly
            float opacity = rule.number(null, "raster-opacity", 1.0f);
            byte alpha = (byte)(opacity*255);

            for (int i = 3; i < raw.capacity(); i+= 4) {
                raw.put(i, alpha);
            }
            raw.rewind();
            drawRasterRGBA(raw, pos, rule);
        }
    }

    ByteBuffer convertToRGBA(ByteBuffer raw, Function<Double,RGB> colormap, Band band, Rule rule) throws IOException {
        byte alpha = (byte)(255*rule.number(null, "raster-opacity", 1f));
        int n = view.getWidth() * view.getHeight();

        ByteBuffer rgba = ByteBuffer.allocate(n*4);
        DataBuffer<Number> db = DataBuffer.create(raw, band.datatype());

        NoData nodata = NoData.create(band.nodata());

        for (int i = 0; i < db.size(); i++) {
            Double val = nodata.valueOrNull(db.get().doubleValue());
            RGB color = colormap.apply(val);
            rgba.put((byte) color.getRed());
            rgba.put((byte) color.getGreen());
            rgba.put((byte) color.getBlue());
            rgba.put(alpha);
        }

        rgba.flip();
        return rgba;
    }

    void render(TileDataset data, RuleList rules) throws IOException {
        if (!canRenderTiles()) {
            throw new UnsupportedOperationException("renderer does not render vector data");
        }
        throw new UnsupportedOperationException("TODO: implement");
    }

    void renderLabels() throws IOException {
        if (labeller == Labeller.NULL) {
            return;
        }
        for (Label l : labels.all()) {
            labeller.render(l);
        }
    }

    void draw(Feature f, Rule rule) throws IOException {
        Geometry g = f.geometry();
        if (g == null) {
            return;
        }

        //g = clipGeometry(g);
        if (g.isEmpty()) {
            return;
        }

        switch(Geom.Type.from(g)) {
        case POINT:
        case MULTIPOINT:
            drawPoint(f, rule, g);
            return;
        case LINESTRING:
        case MULTILINESTRING:
            drawLine(f, rule, g);
            return;
        case POLYGON:
        case MULTIPOLYGON:
            drawPolygon(f, rule, g);
            return;
        default:
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Clips a geometry to the view bounds before rendering.
     */
    protected Geometry clipGeometry(Geometry g) {
        // TODO: doing a full intersection is sub-optimal, look at a more efficient clipping 
        // algorithm, like cohen-sutherland
        return g.intersection(Envelopes.toPolygon(view.getBounds()));
    }

    /**
     * Passes off a new label to the labeller.
     */
    protected boolean insertLabel(Label label) {
        if (labeller == null) {
            throw new IllegalStateException("labeller not set");
        }

        return labeller.layout(label, labels);
    }

    /**
     * Returns a filter that transforms coordinates from world space to screen space.
     */
    protected ViewTransformFilter toScreenTransform() {
        return new ViewTransformFilter(view);
    }

    /**
     * Callback invoked before a new rendering job starts.
     */
    protected void onStart() throws IOException {
    }

    /**
     * Callback invoked before starting to render a new layer.
     */
    protected void onLayerStart(Layer layer) throws IOException {
    }

    /**
     * Callback invoked after a layer has been rendered.
     */
    protected void onLayerFinish(Layer layer) {
    }

    /**
     * Callback invoked after a rendering job has completed.
     */
    protected void onFinish() throws IOException {
    }

    /**
     * Determines if the render can render vector data.
     */
    protected abstract boolean canRenderVectors();

    /**
     * Determines if the render can render raster data.
     */
    protected abstract boolean canRenderRasters();

    /**
     * Determines if the render can render tile data.
     */
    protected abstract boolean canRenderTiles();

    /**
     * Draws the map background.
     */
    protected abstract void drawBackground(RGB color) throws IOException;

    /**
     * Draws a point feature.
     * <p>
     * This method must be implemented by subclasses if {@link #canRenderVectors()}
     * returns true.
     * </p>
     */
    protected void drawPoint(Feature f, Rule rule, Geometry point)  throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Draws a line feature.
     * <p>
     * This method must be implemented by subclasses if {@link #canRenderVectors()}
     * returns true.
     * </p>
     */
    protected void drawLine(Feature f, Rule rule, Geometry line) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Draws a polygon feature.
     * <p>
     * This method must be implemented by subclasses if {@link #canRenderVectors()}
     * returns true.
     * </p>
     */
    protected void drawPolygon(Feature f, Rule rule, Geometry poly)  throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Draws a grayscale raster.
     * <p>
     * This method must be implemented by subclasses if {@link #canRenderRasters()}
     * returns true.
     * </p>
     *
     * @param raster A single band buffer with values interpreted as gray-scale values
     * @param pos The position in the view/window to draw the raster at.
     * @param rule The matching styling rule for the raster.
     */
    protected void drawRasterGray(ByteBuffer raster, Rect pos, Rule rule) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Draws a rgba raster.
     * <p>
     * This method must be implemented by subclasses if {@link #canRenderRasters()}
     * returns true.
     * </p>
     * @param raster A 4 band buffer in the order alpha, blue, green, red
     * @param pos The position in the view/window to draw the raster at.
     * @param rule The matching styling rule for the raster.
     */
    protected void drawRasterRGBA(ByteBuffer raster, Rect pos, Rule rule) throws IOException {
        throw new UnsupportedOperationException();
    }
}
