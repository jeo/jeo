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

import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import io.jeo.data.Dataset;
import io.jeo.data.Handle;
import io.jeo.data.Workspace;
import io.jeo.util.Key;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDataset;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.LukeResponse.FieldTypeInfo;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class SolrWorkspace implements Workspace {

    static final Map<String,Class> TYPE_MAPPINGS = new LinkedHashMap<>();
    static {
        TYPE_MAPPINGS.put("textfield", String.class);
        TYPE_MAPPINGS.put("strfield", String.class);
        TYPE_MAPPINGS.put("intfield", Integer.class);
        TYPE_MAPPINGS.put("trieintfield", Integer.class);
        TYPE_MAPPINGS.put("trielongfield", Long.class);
        TYPE_MAPPINGS.put("longfield", Long.class);
        TYPE_MAPPINGS.put("floatfield", Float.class);
        TYPE_MAPPINGS.put("triefloatfield", Float.class);
        TYPE_MAPPINGS.put("doublefield", Double.class);
        TYPE_MAPPINGS.put("triedoublefield", Double.class);
        TYPE_MAPPINGS.put("boolfield", Boolean.class);
        TYPE_MAPPINGS.put("datefield", Date.class);
        TYPE_MAPPINGS.put("triedatefield", Date.class);
        TYPE_MAPPINGS.put("spatialrecursiveprefixtreefieldtype", Geometry.class);
        TYPE_MAPPINGS.put("geohashfield", Geometry.class);
        TYPE_MAPPINGS.put("geometryfield", Geometry.class);
        TYPE_MAPPINGS.put("latlontype", Point.class);
        TYPE_MAPPINGS.put("pointtype", Point.class);
        TYPE_MAPPINGS.put("bboxfield", Polygon.class);
    }

    static final Map<String,SpatialType> SPATIAL_MAPPINGS = new LinkedHashMap<>();
    static {
        SPATIAL_MAPPINGS.put("spatialrecursiveprefixtreefieldtype", SpatialType.RPT);
        SPATIAL_MAPPINGS.put("geohashfield", SpatialType.RPT);
        SPATIAL_MAPPINGS.put("latlontype", SpatialType.POINT);
        SPATIAL_MAPPINGS.put("pointtype", SpatialType.POINT);
        SPATIAL_MAPPINGS.put("bboxfield", SpatialType.BBOX);
    }

    static final JtsSpatialContext SPATIAL;
    static {
        JtsSpatialContextFactory f = new JtsSpatialContextFactory();
        f.geo = false;
        SPATIAL = f.newSpatialContext();
    }

    SolrClient solr;
    ConcurrentHashMap<String,Class> mappings;

    public SolrWorkspace(SolrClient solr) {
        this.solr = solr;
        mappings = new ConcurrentHashMap<>(TYPE_MAPPINGS);
    }

    @Override
    public Solr driver() {
        return new Solr();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return null;
    }

    @Override
    public Iterable<Handle<Dataset>> list() throws IOException {
        CoreAdminRequest req = new CoreAdminRequest();
        req.setAction(CoreAdminAction.STATUS);

        try {
            CoreAdminResponse rsp = req.process(solr);

            List<Handle<Dataset>> list = new ArrayList<>();
            for (Entry<String, NamedList<Object>> core : rsp.getCoreStatus()) {
                list.add(Handle.to(core.getKey(), this));
            }

            return list;
        } catch (SolrServerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public SolrDataset get(String name) throws IOException {
        return new SolrDataset(name, this);
    }

    @Override
    public VectorDataset create(Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        if (solr != null) {
            solr = null;
        }
    }

    public SolrWorkspace mapSolrType(String type, Class<?> clazz) {
        mappings.put(type.toLowerCase(Locale.ROOT), clazz);
        return this;
    }

    Class classForSolrType(FieldTypeInfo type) {
        Class clazz = lookupBySolrType(type.getClassName(), TYPE_MAPPINGS);
        return clazz != null ? clazz : Object.class;
    }

    SpatialType spatialTypeForSolrType(FieldTypeInfo type) {
        SpatialType t = lookupBySolrType(type.getClassName(), SPATIAL_MAPPINGS);
        return t != null ? t : SpatialType.OTHER;
    }

    <T> T lookupBySolrType(String type, Map<String,T> map) {
        int i = type.lastIndexOf(".");
        type = i > -1 ? type.substring(i+1) : type;
        type = type.toLowerCase(Locale.ROOT);

        T obj = map.get(type);
        if (obj != null) {
            // look for a looser match
            for (String t : map.keySet()) {
                if (type.contains(t)) {
                    obj = map.get(t);
                    break;
                }
            }
        }
        return obj;
    }
}
