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

import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;
import io.jeo.TestData;
import io.jeo.Tests;
import io.jeo.vector.Feature;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.VectorQuery;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static com.spatial4j.core.context.jts.JtsSpatialContext.GEO;
public class LuceneTests {

    public static LuceneDataset setUpStatesData() throws Exception {
        return setUpData(TestData.states(), SpatialField.parse("geom:sdv:rpt:field=geohash levels=4", GEO));
    }

    public static LuceneDataset setUpData(VectorDataset dataset, SpatialField spatial)
        throws IOException {

        Path idxDir = Tests.newTmpDir().toPath().resolve(dataset.name());
        idxDir.toFile().mkdir();

        IndexWriter w = new IndexWriter(FSDirectory.open(idxDir), new IndexWriterConfig(new StandardAnalyzer()));

        for (Feature f : dataset.cursor(new VectorQuery())) {
            Document doc = new Document();
            for (Map.Entry<String,Object> kvp : f.map().entrySet()) {
                String key = kvp.getKey();
                Object val = kvp.getValue();

                if (val instanceof Geometry) {
                    JtsGeometry shp = GEO.makeShape((Geometry) val);
                    shp.index();

                    spatial.storage.write(shp, doc);
                    for (IndexableField fld : spatial.index.strategy().createIndexableFields(shp)) {
                        doc.add(fld);
                    }
                }
                else if (val instanceof Integer) {
                    doc.add(intField(key, val));
                }
                else if (val instanceof Double) {
                    doc.add(doubField(key, val));
                }
                else {
                    doc.add(strField(key, val));
                }
            }

            w.addDocument(doc);
        }
        w.commit();
        w.close();

        return Lucene.open(new LuceneOpts(idxDir.toFile()).spatialField(spatial));
    }

    static TextField strField(String name, Object obj) {
        return new TextField(name, obj.toString(), Store.YES);
    }

    static IntField intField(String name, Object obj) {
        return new IntField(name, ((Number)obj).intValue(), Store.YES);
    }

    static DoubleField doubField(String name, Object obj) {
        return new DoubleField(name, ((Number)obj).doubleValue(), Store.YES);
    }
}
