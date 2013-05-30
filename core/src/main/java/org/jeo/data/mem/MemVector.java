package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class MemVector implements VectorData {

    Schema schema;
    List<Feature> features = new ArrayList<Feature>();
    
    public MemVector(Schema schema) {
        this.schema = schema;
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
    public Schema getSchema() throws IOException {
        return schema;
    }
    
    @Override
    public long count(Query q) throws IOException {
        if (q == null) {
            return features.size();
        }
    
        Cursor<Feature> cursor = Cursors.create(features);
        if (q.getBounds() != null && !q.getBounds().isNull()) {
            cursor = Cursors.intersects(cursor, q.getBounds());
        }
    
        return Cursors.size(q.apply(cursor));
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        Cursor<Feature> cursor = new MemCursor(q.getMode(), this);
    
        if (q.getBounds() != null && !q.getBounds().isNull()) {
            cursor = Cursors.intersects(cursor, q.getBounds());
        }
    
        return q.apply(cursor);
    }

    public void add(Feature f) {
        features.add(f);
    }

    @Override
    public void dispose() {
    }
}
