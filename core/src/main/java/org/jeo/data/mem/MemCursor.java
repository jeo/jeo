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
package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.DiffFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.FeatureCursor;

public class MemCursor extends FeatureCursor {

    MemVector dataset;
    Cursor<Feature> cursor;
    Feature curr;

    MemCursor(Mode mode, List<Feature> features, MemVector dataset) {
        super(mode);
        this.dataset = dataset;
        cursor = Cursors.create(features);
    }
    
    @Override
    public boolean hasNext() throws IOException {
        if (mode == APPEND) {
            return true;
        }
    
        return cursor.hasNext();
    }
    
    @Override
    public Feature next() throws IOException {
        if (mode == APPEND) {
            curr = new BasicFeature(null, new ArrayList<Object>(), dataset.schema());
        }
        else {
            curr = cursor.next();
            if (mode == UPDATE) {
                curr = new DiffFeature(curr);
            }
        }
        return curr;
    }
    
    @Override
    protected void doWrite() throws IOException {
        if (mode == APPEND) {
            dataset.add(curr);
        }
        else {
            dataset.modify((DiffFeature)curr);
        }
    }
    
    @Override
    protected void doRemove() throws IOException {
        Feature f = ((DiffFeature)curr).getDelegate();
        dataset.remove(f);
    }
    
    @Override
    public void close() throws IOException {
    }
}
