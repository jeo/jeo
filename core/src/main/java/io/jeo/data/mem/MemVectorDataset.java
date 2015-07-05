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
package io.jeo.data.mem;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.jeo.geom.Envelopes;
import io.jeo.vector.DiffFeature;
import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;
import io.jeo.vector.Field;
import io.jeo.vector.VectorQueryPlan;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.Feature;
import io.jeo.vector.Schema;
import io.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class MemVectorDataset implements VectorDataset {

    Schema schema;
    Map<String,Feature> features = new LinkedHashMap<>();
    SpatialIndex index;

    public MemVectorDataset(Schema schema) {
        this.schema = schema;
        index = new Quadtree();
    }

    public Memory driver() {
        return new Memory();
    }

    public Map<Key<?>,Object> driverOptions() {
        return Collections.emptyMap();
    }
    
    Iterable<Feature> features() {
        return features.values();
    }

    @Override
    public String name() {
        return schema.name();
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
    
        for (Feature f : features()) {
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
        return read(q).count();
    }

    @Override
    public FeatureCursor read(VectorQuery q) throws IOException {
        VectorQueryPlan qp = new VectorQueryPlan(q);

        Iterable<Feature> features = features();
        if (!Envelopes.isNull(q.bounds())) {
            features = query(q.bounds());
            qp.bounded();
        }

        return qp.apply(new MemFeatureCursor(features));
    }

    @Override
    public FeatureWriteCursor update(VectorQuery q) throws IOException {
        return new MemFeatureWriteCursor(read(q).iterator(), this);
    }

    @Override
    public FeatureAppendCursor append(VectorQuery q) throws IOException {
        return new MemFeatureAppendCursor(this);
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
        features.put(f.id(), f);

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
        if (geo != null && f.changed().containsKey(geo.name())) {
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
        add(f.getDelegate());
    }

    @Override
    public void close() {
    }

}
