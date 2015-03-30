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

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.jeo.vector.Feature;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.Schema;

import java.io.IOException;

public class LuceneCursor extends FeatureCursor  {

    TopDocs results;
    IndexSearcher searcher;
    LuceneDataset dataset;
    int count;

    public LuceneCursor(TopDocs results, IndexSearcher searcher, LuceneDataset dataset) {
        this.results = results;
        this.searcher = searcher;
        this.dataset = dataset;
        this.count = 0;
    }

    @Override
    public boolean hasNext() throws IOException {
        return count < results.scoreDocs.length;
    }

    @Override
    public Feature next() throws IOException {
        int docId = results.scoreDocs[count++].doc;
        Document doc = searcher.doc(docId);
        return dataset.feature(doc, docId);
    }

    @Override
    public void close() throws IOException {
    }
}
