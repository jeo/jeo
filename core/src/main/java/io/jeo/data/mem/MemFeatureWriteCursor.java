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
import java.util.Iterator;

import io.jeo.vector.DiffFeature;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureWriteCursor;

public class MemFeatureWriteCursor extends FeatureWriteCursor {

    MemVectorDataset dataset;
    Iterator<Feature> it;
    DiffFeature curr;

    MemFeatureWriteCursor(Iterator<Feature> features, MemVectorDataset dataset) {
        this.it = features;
        this.dataset = dataset;
    }
    
    @Override
    public boolean hasNext() throws IOException {
        return it.hasNext();
    }
    
    @Override
    public Feature next() throws IOException {
        return curr = new DiffFeature(it.next());
    }

    @Override
    public MemFeatureWriteCursor write() throws IOException {
        dataset.modify(curr);
        return this;
    }

    @Override
    public MemFeatureWriteCursor remove() throws IOException {
        dataset.remove(curr.getDelegate());
        return this;
    }
    
    @Override
    public void close() {
    }
}
