package org.jeo.map.render;

import static org.jeo.map.CartoCSS.*;

import java.io.IOException;

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

public abstract class BaseRenderer {

    static final Logger LOG = LoggerFactory.getLogger(BaseRenderer.class);

    protected View view;

    protected LabelIndex labels = new LabelIndex();
    protected Labeller labeller;

    public void init(View view) {
        this.view = view;
        this.labeller = createLabeller();
    }

    protected Labeller createLabeller() {
        return Labeller.NULL;
    }

    public void render() {
        LOG.debug("Rendering map at " + view.getBounds());
        onStart();

        // background
        renderBackground();
        for (Layer l : view.getMap().getLayers()) {
            if (!l.isVisible()) {
                continue;
            }

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
        }

        //labels
        renderLabels();

        LOG.debug("Rendering complete");
        onFinish();
    }

    void renderBackground() {
        RuleList rules = view.getMap().getStyle().getRules().selectByName("Map", false);
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

    void renderLabels() {
        if (labeller == Labeller.NULL) {
            return;
        }
        for (Label l : labels.all()) {
            labeller.render(l);
        }
    }

    void render(VectorDataset data, RuleList rules, Filter<Feature> filter) {
        try {
            // build up the data query
            Query q = new Query();

            // bounds, we may have to reproject it
            Envelope bbox = view.getBounds();
            CoordinateReferenceSystem crs = data.crs();

            // reproject
            if (crs != null) {
                if (view.getCRS() != null && !Proj.equal(view.getCRS(), data.crs())) {
                    q.reproject(view.getCRS());
                    bbox = Proj.reproject(bbox, view.getCRS(), crs);
                }
            }
            else {
                LOG.debug(
                    "Layer "+data.getName()+" specifies no projection, assuming map projection");
            }

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
        } catch (IOException e) {
            LOG.error("Error querying layer " + data.getName(), e);
        }
    }

    void render(TileDataset data, RuleList rules) {
    }

    void draw(Feature f, Rule rule) {
        Geometry g = f.geometry();
        if (g == null) {
            return;
        }

        g = clipGeometry(g);
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

    protected Geometry clipGeometry(Geometry g) {
        // TODO: doing a full intersection is sub-optimal, look at a more efficient clipping 
        // algorithm, like cohen-sutherland
        return g.intersection(Envelopes.toPolygon(view.getBounds()));
    }

    protected boolean insertLabel(Label label) {
        if (labeller == null) {
            throw new IllegalStateException("labeller not set");
        }

        return labeller.layout(label, labels);
    }

    protected ViewTransformFilter toScreenTransform() {
        return new ViewTransformFilter(view);
    }

    protected void onStart() {
    }

    protected void onFinish() {
    }

    protected abstract void drawBackground(RGB color);

    protected abstract void drawPoint(Feature f, Rule rule, Geometry point);

    protected abstract void drawLine(Feature f, Rule rule, Geometry line);

    protected abstract void drawPolygon(Feature f, Rule rule, Geometry poly);
}
