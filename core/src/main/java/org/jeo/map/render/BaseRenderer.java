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

import org.jeo.data.Dataset;
import org.jeo.data.Query;
import org.jeo.data.TileDataset;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Feature;
import org.jeo.filter.Filter;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.map.Layer;
import org.jeo.map.Map;
import org.jeo.map.RGB;
import org.jeo.map.Rule;
import org.jeo.map.RuleList;
import org.jeo.map.View;
import org.jeo.proj.Proj;
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

            if (data instanceof VectorDataset) {
                for (RuleList ruleList : rules.zgroup()) {
                    render((VectorDataset)data, ruleList, filter);
                }
            }
            else {
                render((TileDataset)data, rules);
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

    void render(TileDataset data, RuleList rules) throws IOException {
        throw new UnsupportedOperationException("rendering tile datasets not implemented");
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
     * Draws the map background.
     */
    protected abstract void drawBackground(RGB color) throws IOException;

    /**
     * Draws a point feature.
     */
    protected abstract void drawPoint(Feature f, Rule rule, Geometry point)  throws IOException;

    /**
     * Draws a line feature.
     */
    protected abstract void drawLine(Feature f, Rule rule, Geometry line) throws IOException;

    /**
     * Draws a line feature.
     */
    protected abstract void drawPolygon(Feature f, Rule rule, Geometry poly)  throws IOException;
}
