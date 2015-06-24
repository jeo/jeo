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
package io.jeo.lucene;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import io.jeo.data.Driver;
import io.jeo.data.FileData;
import io.jeo.filter.Filter;
import io.jeo.filter.FilterSplitter;
import io.jeo.filter.Filters;
import io.jeo.geom.Envelopes;
import io.jeo.proj.Proj;
import io.jeo.util.Key;
import io.jeo.util.Optional;
import io.jeo.util.Pair;
import io.jeo.vector.BasicFeature;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Field;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorQueryPlan;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuceneDataset implements VectorDataset, FileData {

    static Logger LOG = LoggerFactory.getLogger(LuceneDataset.class);

    static final String SPATIAL_STRATEGY = "spatialStrategy";
    static final String SPATIAL_FIELD = "spatialField";

    LuceneOpts opts;
    DirectoryReader reader;
    Schema schema;

    public LuceneDataset(LuceneOpts opts) throws IOException {
        this.opts = opts;
        this.reader = open();
    }

    @Override
    public File file() {
        return opts.file();
    }

    @Override
    public Driver<?> driver() {
        return new Lucene();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return opts.toMap();
    }

    @Override
    public String name() {
        return file().getName();
    }

    @Override
    public Schema schema() {
        if (schema == null) {
            schema = buildSchema();
        }
        return schema;
    }

    @Override
    public CoordinateReferenceSystem crs() {
        // TODO: handle different projections?
        return Proj.EPSG_4326;
    }

    @Override
    public Envelope bounds() throws IOException {
        return cursor(new VectorQuery()).bounds();
    }

    @Override
    public long count(VectorQuery q) throws IOException {
        if (q.isAll()) {
            return q.adjustCount(reader.numDocs());
        }
        else {
            VectorQueryPlan qp = new VectorQueryPlan(q);
            Query lq = createQuery(q, qp);

            if (q.isFiltered()) {
                if (!qp.isFiltered()) {
                    // can't optimize
                    return qp.apply(cursor(lq, q.limit())).count();
                }
            }

            Pair<TopDocs,IndexSearcher> result = search(lq, q.limit());
            return q.adjustCount(result.first.totalHits);
        }
    }

    public FeatureCursor cursor(Query lq, Integer n) throws IOException {
        Pair<TopDocs,IndexSearcher> result = search(lq, n);
        return new LuceneCursor(result.first, result.second, this);
    }

    @Override
    public FeatureCursor cursor(VectorQuery q) throws IOException {
        VectorQueryPlan qp = new VectorQueryPlan(q);
        Query lq;

        if (q.isAll()) {
            lq = new MatchAllDocsQuery();
        }
        else {
            lq = createQuery(q, qp);
        }

        return qp.apply(cursor(lq, q.limit()));
    }

    Pair<TopDocs, IndexSearcher> search(Query lq, Integer n) throws IOException {
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(lq, n != null && n > 0 ? n.intValue() : Integer.MAX_VALUE);

        return Pair.of(docs, searcher);
    }

    Query createQuery(VectorQuery q, VectorQueryPlan qp) throws IOException {
        BooleanQuery lq = new BooleanQuery();

        Envelope bounds = q.bounds();
        if (!Envelopes.isNull(bounds)) {
            Rectangle rect =
                ctx().makeRectangle(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
            Query spatial = null;
            for (Field fld : schema()) {
                if (fld.geometry()) {
                    SpatialField sfld = spatialField(fld);
                    Query sq = sfld.index.strategy().makeQuery(new SpatialArgs(SpatialOperation.Intersects, rect));
                    if (spatial == null) {
                        spatial = sq;
                    }
                    else {
                        if (!(spatial instanceof BooleanQuery)) {
                            BooleanQuery bq = new BooleanQuery();
                            bq.add(spatial, Occur.SHOULD);
                            spatial = bq;
                        }
                        ((BooleanQuery)spatial).add(sq, Occur.SHOULD);
                    }
                }
            }
            if (spatial != null) {
                qp.bounded();
                lq.add(spatial, Occur.MUST);
            }
            else {
                LOG.debug("bounds constraint ignored, no spatial field found");
            }
        }

        if (!Filters.isTrueOrNull(q.filter())) {
            Pair<Filter,Filter> filters = new FilterSplitter(new LuceneQueryQualifier(this)).split(q.filter());
            if (!Filters.isTrueOrNull(filters.first)) {
                try {
                    lq.add(LuceneQueryEncoder.encode(filters.first, this), Occur.MUST);
                    qp.filtered(filters.second);
                } catch (Exception e) {
                    LOG.debug("Unable to encode lucene query: {}", filters.first, e);
                }
            }
        }

        Query rq = lq;
        if (lq.getClauses().length == 0) {
            rq = new MatchAllDocsQuery();
        }
        if (lq.getClauses().length == 1) {
            BooleanClause clause = lq.getClauses()[0];
            if (clause.getOccur() == Occur.MUST) {
                rq = clause.getQuery();
            }
        }

        qp.limited();
        return rq;
    }

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.debug("error closing index reader", e);
            }
            reader = null;
        }
    }

    DirectoryReader open() throws IOException {
        Directory dir = FSDirectory.open(file().toPath());
        return DirectoryReader.open(dir);
    }

    Map<String,SpatialField> spatialFields() {
        Map<String,SpatialField> spatialFields = new HashMap<>();
        for (SpatialField fld : opts.spatialFields()) {
            spatialFields.put(fld.name, fld);
        }
        return spatialFields;
    }

    Schema buildSchema() {
        SchemaBuilder sb = Schema.build(name());

        Map<String,SpatialField> spatialFields = spatialFields();

        for (LeafReaderContext leafCtx : reader.getContext().leaves()) {
            LeafReader lr = leafCtx.reader();
            for (FieldInfo fi : lr.getFieldInfos()) {
                SpatialField sfld = spatialFields.get(fi.name);
                if (sfld != null) {
                    sb.property(SPATIAL_FIELD, sfld);
                    sb.field(fi.name, Geometry.class, crs());
                }
                else {
                    sb.field(fi.name, Object.class);
                }
            }
        }

        return sb.schema();
    }

    Feature feature(Document doc, int docId) throws IOException {
        Schema schema = schema();
        List<Object> values = new ArrayList<>();

        for (Field fld : schema) {
            if (fld.geometry()) {
                SpatialField sfld = spatialField(fld);
                values.add(toGeometry(sfld.storage.read(doc, docId, reader)));
            }
            else {
                IndexableField f = doc.getField(fld.name());
                if (f == null) {
                    values.add(null);
                    continue;
                }

                Number n = f.numericValue();
                if (n != null) {
                    if (n instanceof Integer) {
                        values.add(n.intValue());
                    } else if (n instanceof Long) {
                        values.add(n.longValue());
                    } else if (n instanceof Float) {
                        values.add(n.floatValue());
                    } else {
                        values.add(n.doubleValue());
                    }
                } else {
                    values.add(f.stringValue());
                }
            }

        }

        return new BasicFeature(String.valueOf(docId), values, schema);
    }

    SpatialField spatialField(Field fld) {
        return Optional.of(fld.property(SPATIAL_FIELD, SpatialField.class)).get();
    }

    JtsSpatialContext ctx() {
        return opts.spatialContext();
    }

    Analyzer analyzer() {
        return opts.analyzer();
    }

    Geometry toGeometry(Shape shp) {
        if (shp instanceof JtsGeometry) {
            return ((JtsGeometry) shp).getGeom();
        }

        GeometryFactory gf = ctx().getGeometryFactory();
        if (shp instanceof Point) {
            Point p = (Point) shp;
            return gf.createPoint(new Coordinate(p.getX(), p.getY()));
        }

        Rectangle box = shp.getBoundingBox();
        return gf.toGeometry(new Envelope(box.getMinX(), box.getMaxX(), box.getMinY(), box.getMaxY()));
    }
}
