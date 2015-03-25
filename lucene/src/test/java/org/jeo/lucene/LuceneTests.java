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
package org.jeo.lucene;

import com.spatial4j.core.shape.jts.JtsGeometry;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.serialized.SerializedDVStrategy;
import org.apache.lucene.store.FSDirectory;
import org.jeo.TestData;
import org.jeo.Tests;
import org.jeo.vector.Feature;
import org.jeo.vector.VectorQuery;

import java.nio.file.Path;

import static com.spatial4j.core.context.jts.JtsSpatialContext.GEO;
public class LuceneTests {

    public static LuceneDataset setupStatesData() throws Exception {

        Path idxDir = Tests.newTmpDir().toPath().resolve("states");
        idxDir.toFile().mkdir();

        SpatialStrategy geohash =
            new RecursivePrefixTreeStrategy(new GeohashPrefixTree(GEO, 6), "geohash");
        SpatialStrategy geom = new SerializedDVStrategy(GEO, "geom");

        IndexWriter w = new IndexWriter(FSDirectory.open(idxDir), new IndexWriterConfig(new StandardAnalyzer()));

        for (Feature f : TestData.states().cursor(new VectorQuery())) {
            Document doc = new Document();
            doc.add(strField("STATE_NAME", f));
            doc.add(strField("STATE_ABBR", f));
            doc.add(intField("SAMP_POP", f));
            doc.add(doubField("P_MALE", f));
            doc.add(doubField("P_FEMALE", f));

            JtsGeometry shp = GEO.makeShape(f.geometry());
            shp.index();

            for (IndexableField fld : geohash.createIndexableFields(shp)) {
                doc.add(fld);
            }
            for (IndexableField fld : geom.createIndexableFields(shp)) {
                doc.add(fld);
            }

            w.addDocument(doc);
        }
        w.commit();
        w.close();


        return Lucene.open(new LuceneOpts(idxDir.toFile())
            .spatialField("geom:sdv:rpt:field=geohash levels=4"));
      //    .spatialField("geom:none:rpt field=geohash levels=4 store=sdv"));
      //    .spatialField("geom:wkt:rpt field=wkt levels=4 store=sdv"));
    }

    static TextField strField(String name, Feature f) {
        return new TextField(name, f.get(name).toString(), Store.YES);
    }

    static IntField intField(String name, Feature f) {
        return new IntField(name, ((Number)f.get(name)).intValue(), Store.YES);
    }

    static DoubleField doubField(String name, Feature f) {
        return new DoubleField(name, ((Number)f.get(name)).doubleValue(), Store.YES);
    }
}
