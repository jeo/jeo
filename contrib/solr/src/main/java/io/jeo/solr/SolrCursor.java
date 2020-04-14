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

import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.spatial4j.core.shape.jts.JtsPoint;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Field;
import io.jeo.vector.MapFeature;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SolrCursor extends FeatureCursor {

    static Logger LOG = LoggerFactory.getLogger(SolrCursor.class);

    Iterator<SolrDocument> it;
    SolrDataset dataset;
    List<Field> geomFields;
    boolean stripDocId = false;

    public SolrCursor(QueryResponse query, SolrDataset dataset) throws IOException {
        this.dataset = dataset;

        it = query.getResults().iterator();
        geomFields = new ArrayList<>();
        for (Field f : dataset.schema()) {
            if (f.geometry()) {
                geomFields.add(f);
            }
        }
    }

    public SolrCursor stripDocId(boolean stripDocId) {
        this.stripDocId = stripDocId;
        return this;
    }

    @Override
    public boolean hasNext() throws IOException {
        return it.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        SolrDocument doc = it.next();

        Object id = doc.get(dataset.key);
        for (Field g : geomFields) {
            convertToGeom(g, doc);
        }

        if (stripDocId) {
            doc.remove(dataset.key);
        }
        return new MapFeature(id!=null?id.toString():null, doc);
    }

    void convertToGeom(Field g, SolrDocument doc) {
        Object val = doc.get(g.name());

        if (val != null) {
            Geometry geom = null;
            if (val instanceof String) {
                // try to parse as wkt directly
                try {
                    geom = new WKTReader().read(val.toString());
                } catch (ParseException e) {
                    LOG.debug("Error parsing geometry field as wkt", e);
                }
            }

            if (geom == null) {
                // parse as a spatial4j shape
                Shape shp = SolrWorkspace.SPATIAL.getFormats().read(val.toString());
                if (shp != null) {
                    if (shp instanceof JtsPoint) {
                        geom = ((JtsPoint)shp).getGeom();
                    }
                    else if (shp instanceof JtsGeometry) {
                        geom = ((JtsGeometry)shp).getGeom();
                    }
                }
            }

            if (geom != null) {
                doc.setField(g.name(), geom);
            }
            else {
                throw new IllegalArgumentException("Unable to create geometry from: " + val);
            }
        }
    }

    @Override
    public void close() throws IOException {
    }
}
