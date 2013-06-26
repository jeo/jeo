package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.data.VectorData;
import org.jeo.feature.DiffFeature;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geom.Envelopes;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class MemVector implements VectorData {

    Schema schema;
    List<Feature> features = new ArrayList<Feature>();
    SpatialIndex index;

    public MemVector(Schema schema) {
        this.schema = schema;
        index = new Quadtree();
    }

    public Memory getDriver() {
        return new Memory();
    };

    List<Feature> getFeatures() {
        return features;
    }

    @Override
    public String getName() {
        return schema.getName();
    }
    
    @Override
    public String getTitle() {
        return getName();
    }
    
    @Override
    public String getDescription() {
        return null;
    }
    
    @Override
    public CoordinateReferenceSystem getCRS() {
        return schema.crs();
    }
    
    @Override
    public Envelope bounds() throws IOException {
        if (schema.geometry() == null) {
            return null;
        }
    
        Envelope e = new Envelope();
        e.setToNull();
    
        if (features.isEmpty()) {
            return e;
        }
    
        for (Feature f : features) {
            Geometry g = f.geometry();
            if (g != null) {
                e.expandToInclude(g.getEnvelopeInternal());
            }
        }
    
        return e;
    }
    
    @Override
    public Schema getSchema() {
        return schema;
    }
    
    @Override
    public long count(Query q) throws IOException {
        return Cursors.size(cursor(q));
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        QueryPlan qp = new QueryPlan(q);

        List<Feature> features = this.features;
        if (!Envelopes.isNull(q.getBounds())) {
            features = query(q.getBounds()); 
            qp.bounded();
        }

        return qp.apply(new MemCursor(q.getMode(), features, this));
    }

    List<Feature> query(Envelope bounds) {
        List<Feature> features = index.query(bounds);
        for (Iterator<Feature> it = features.iterator(); it.hasNext(); ) {
            Feature f = it.next();
            if (!bounds.intersects(f.geometry().getEnvelopeInternal())) {
                it.remove();
            }
        }

        return features;
    }

    public void add(Feature f) {
        features.add(f);

        Geometry g = f.geometry();
        if (g != null) {
            index.insert(g.getEnvelopeInternal(), f);
        }
    }

    public void remove(Feature f) {
        features.remove(f);

        Geometry g = f.geometry();
        if (g != null) {
            index.remove(g.getEnvelopeInternal(), f);
        }
    }

    void modify(DiffFeature f) {
        Feature pre = f.getDelegate();

        Field geo = schema.geometry(); 
        if (geo != null && f.getChanged().containsKey(geo.getName())) {
            Geometry g1 = pre.geometry();
            Geometry g2 = f.geometry();

            if (g1 != null) {
                index.remove(g1.getEnvelopeInternal(), pre);
            }

            if (g2 != null) {
                index.insert(g2.getEnvelopeInternal(), pre);
            }
        }

        f.apply();
    }

    @Override
    public void close() {
    }

}
