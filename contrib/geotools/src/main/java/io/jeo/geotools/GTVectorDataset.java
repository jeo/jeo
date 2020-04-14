/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.geotools;

import io.jeo.data.Driver;
import io.jeo.geom.Bounds;
import io.jeo.util.Key;
import io.jeo.util.Optional;
import io.jeo.util.Supplier;
import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorQueryPlan;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class GTVectorDataset implements VectorDataset {

    SimpleFeatureSource source;
    GTWorkspace workspace;

    public GTVectorDataset(SimpleFeatureSource source, GTWorkspace workspace) {
        this.source = source;
        this.workspace = workspace;
    }

    @Override
    public Driver<?> driver() {
        return workspace.driver();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return workspace.driverOptions();
    }

    @Override
    public Schema schema() {
        return GT.schema(source.getSchema());
    }

    @Override
    public String name() {
        return source.getName().getLocalPart();
    }

    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        org.opengis.referencing.crs.CoordinateReferenceSystem crs =
            source.getSchema().getCoordinateReferenceSystem();
        return crs != null ? GT.crs(crs) : null;
    }

    @Override
    public Bounds bounds() throws IOException {
        return new Bounds(source.getBounds());
    }

    @Override
    public long count(VectorQuery q) throws IOException {
        Query gtq = convert(q, new VectorQueryPlan(q));
        long count = source.getCount(gtq);
        if (count == -1) {
            // geotools contract says count "may" return a value
            count = DataUtilities.count(source.getFeatures(gtq));
        }

        return count;
    }

    @Override
    public FeatureCursor read(VectorQuery q) throws IOException {
        VectorQueryPlan qp = new VectorQueryPlan(q);
        Query gtq = convert(q, qp);

        SimpleFeatureIterator it = source.getFeatures(gtq).features();
        return qp.apply(new GTFeatureCursor(it, this));
    }

    @Override
    public FeatureWriteCursor update(VectorQuery q) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureAppendCursor append(VectorQuery q) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
    }

    Optional<GeometryDescriptor> defaultGeom() {
        return Optional.of(source.getSchema().getGeometryDescriptor());
    }

    Query convert(VectorQuery q, VectorQueryPlan qp) {
        Query gtq = new Query(name());

        if (!q.isAll()) {
            if (!Bounds.isNull(q.bounds())) {
                GeometryDescriptor geom = defaultGeom().orElseThrow(new Supplier<RuntimeException>() {
                    @Override
                    public RuntimeException get() {
                        throw new IllegalStateException("No default geometry, unable to convert bounds filter");
                    }
                });
                gtq.setFilter(GT.filterFactory.bbox(GT.filterFactory.property(geom.getLocalName()),
                    new ReferencedEnvelope(q.bounds(), geom.getCoordinateReferenceSystem())));
                qp.bounded();
            }

            if (q.isFiltered()) {
                Filter f = GT.filter(q.filter(), source.getSchema());
                Filter g = gtq.getFilter();
                if (g != null && g != Filter.INCLUDE) {
                    f = GT.filterFactory.and(g, f);
                }
                gtq.setFilter(f);
                qp.filtered();
            }

            if (!q.fields().isEmpty()) {
                gtq.setPropertyNames(new ArrayList<>(q.fields()));
                qp.fields();
            }

            if (q.limit() != null) {
                gtq.setMaxFeatures(q.limit());
                qp.limited();
            }

            if (q.offset() != null) {
                gtq.setStartIndex(q.offset());
                qp.offsetted();
            }
        }

        return gtq;
    }
}
