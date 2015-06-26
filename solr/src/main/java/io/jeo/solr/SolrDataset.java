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
package io.jeo.solr;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import io.jeo.data.Driver;
import io.jeo.filter.Filter;
import io.jeo.filter.FilterSplitter;
import io.jeo.filter.Filters;
import io.jeo.geom.Envelopes;
import io.jeo.proj.Proj;
import io.jeo.util.Key;
import io.jeo.util.Pair;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Field;
import io.jeo.vector.Schema;
import io.jeo.vector.SchemaBuilder;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorQueryPlan;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.LukeResponse.FieldInfo;
import org.apache.solr.client.solrj.response.LukeResponse.FieldTypeInfo;
import org.apache.solr.common.util.NamedList;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;

public class SolrDataset implements VectorDataset {

    static String SPATIAL_TYPE = "spatialType";

    static Logger LOG = LoggerFactory.getLogger(SolrDataset.class);

    SolrWorkspace workspace;
    SolrClient solr;
    String core;

    Schema schema;
    String key;

    public SolrDataset(String core, SolrWorkspace workspace) {
        this.workspace = workspace;
        this.core = core;
        this.solr = workspace.solr;
    }

    @Override
    public String name() {
        return core;
    }

    @Override
    public Schema schema() throws IOException {
        if (schema == null) {
            schema = buildSchema();
        }
        return schema;
    }

    @Override
    public Driver<?> driver() {
        return workspace.driver();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return workspace.driverOptions();
    }

    Schema buildSchema() throws IOException {
        LukeRequest req = runRequest(new LukeRequest());
        req.setShowSchema(true);

        LukeResponse rsp;
        try {
            rsp = req.process(solr);
        } catch (SolrServerException e) {
            throw new IOException(e);
        }

        // figure out unique key field
        key = findUniqueKey(rsp);

        SchemaBuilder sb = Schema.build(core);
        for (FieldInfo fld : rsp.getFieldInfo().values()) {
            FieldTypeInfo fldType = rsp.getFieldTypeInfo(fld.getType());
            Class clazz = workspace.classForSolrType(fldType);

            boolean isGeom = Geometry.class.isAssignableFrom(clazz);
            if (isGeom) {
                sb.property(SPATIAL_TYPE, workspace.spatialTypeForSolrType(fldType));
            }
            sb.field(fld.getName(), clazz, isGeom ? crs() : null);
        }

        return sb.schema();
    }

    String findUniqueKey(LukeResponse rsp) {
        NamedList schema = (NamedList) rsp.getResponse().get("schema");
        return (String) schema.get("uniqueKeyField");
    }

    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        // TODO: handle different projections?
        return Proj.EPSG_4326;
    }

    @Override
    public Envelope bounds() throws IOException {
        return read(VectorQuery.all()).bounds();
    }

    @Override
    public long count(VectorQuery q) throws IOException {
        if (q.isAll()) {
            LukeRequest req = runRequest(new LukeRequest());
            try {
                LukeResponse rsp = req.process(solr);
                return q.adjustCount(rsp.getNumDocs());
            } catch (SolrServerException e) {
                throw new IOException(e);
            }
        }

        VectorQueryPlan qp = new VectorQueryPlan(q);
        return qp.apply(read(q)).count();
    }

    @Override
    public FeatureCursor read(VectorQuery q) throws IOException {
        VectorQueryPlan qp = new VectorQueryPlan(q);
        SolrQuery sq = new SolrQuery("*:*");

        if (!q.isAll()) {
            encodeQuery(sq, q, qp);
        }

        // TODO: use cursors for paging?

        if (q.offset() != null) {
            sq.setStart(q.offset());
            qp.offsetted();
        }

        if (q.limit() != null) {
            sq.setRows(q.limit());
            qp.limited();
        }
        else {
            sq.setRows(100);
        }

        if (q.fields().isEmpty()) {
            sq.addField("*");
        }
        else {
            for (String fld : q.fields()) {
                sq.addField(fld);
            }
        }
        if (key != null) {
            sq.addField(key);
        }

        LOG.debug("{}", sq);

        try {
            return qp.apply(new SolrCursor(runRequest(new QueryRequest(sq)).process(solr), this));
        } catch (SolrServerException e) {
            throw new IOException(e);
        }

    }

    void encodeQuery(SolrQuery sq, VectorQuery q, VectorQueryPlan qp) throws IOException {
        if (!Envelopes.isNull(q.bounds())) {
            Envelope e = q.bounds();
            for (Field fld : schema()) {
                if (fld.geometry()) {
                    sq.addFilterQuery(format(Locale.ROOT, "%s:\"Intersects(ENVELOPE(%f, %f, %f, %f))\"",
                        fld.name(), e.getMinX(), e.getMaxX(), e.getMaxY(), e.getMinY()));
                }
            }
            qp.bounded();
        }

        if (!Filters.isTrueOrNull(q.filter())) {
            Pair<Filter,Filter> filters = new FilterSplitter(new SolrQueryQualifier(schema())).split(q.filter());
            if (!Filters.isTrueOrNull(filters.first)) {
                try {
                    filters.first.accept(new SolrQueryEncoder(sq, this), null);
                    qp.filtered(filters.second);
                } catch (Exception e) {
                    LOG.debug("Unable to encode solr query: {}", filters.first, e);
                }
            }
        }
    }

    @Override
    public void close() {
    }


    <T extends SolrRequest> T runRequest(T req) {
        req.setPath("/" + core + req.getPath());
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} {}", req.getPath(), req.getParams());
        }
        return req;
    }
}
