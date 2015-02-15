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
package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.VectorQuery;
import org.jeo.vector.VectorQueryPlan;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.DiffFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;
import org.jeo.vector.Schema;
import org.jeo.geom.Envelopes;
import org.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class MemVector implements VectorDataset {

    Schema schema;
    List<Feature> features = new ArrayList<Feature>();
    SpatialIndex index;

    public MemVector(Schema schema) {
        this.schema = schema;
        index = new Quadtree();
    }

    public Memory getDriver() {
        return new Memory();
    }

    public Map<Key<?>,Object> getDriverOptions() {
        return Collections.emptyMap();
    }
    
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
    public CoordinateReferenceSystem crs() {
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
    public Schema schema() {
        return schema;
    }
    
    @Override
    public long count(VectorQuery q) throws IOException {
        return cursor(q).count();
    }

    @Override
    public FeatureCursor cursor(VectorQuery q) throws IOException {
        VectorQueryPlan qp = new VectorQueryPlan(q);

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
