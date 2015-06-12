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
import io.jeo.vector.BasicFeature;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Field;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SolrCursor extends FeatureCursor {

    Iterator<SolrDocument> it;
    SolrDataset dataset;
    List<Field> geomFields;

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

        return new BasicFeature(id!=null?id.toString():null, doc, dataset.schema());
    }

    void convertToGeom(Field g, SolrDocument doc) {
        Object val = doc.get(g.name());
        if (val != null) {
            Shape shp = SolrWorkspace.SPATIAL.getFormats().read(val.toString());
            if (shp != null) {
                Geometry geom = null;
                if (shp instanceof JtsPoint) {
                    geom = ((JtsPoint)shp).getGeom();
                }
                else if (shp instanceof JtsGeometry) {
                    geom = ((JtsGeometry)shp).getGeom();
                }

                if (geom != null) {
                    doc.setField(g.name(), geom);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
    }
}
