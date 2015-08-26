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
package io.jeo.data.mem;

import io.jeo.vector.Feature;
import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.ListFeature;

import java.io.IOException;

public class MemFeatureAppendCursor extends FeatureAppendCursor {

    MemVectorDataset dataset;
    Feature curr;

    public MemFeatureAppendCursor(MemVectorDataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public Feature next() throws IOException {
        return curr = new ListFeature(dataset.schema());
    }

    @Override
    public MemFeatureAppendCursor write() throws IOException {
        dataset.add(curr);
        return this;
    }

    @Override
    public void close() {
    }

}
