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
package org.jeo.geogit;

import java.io.IOException;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;

public class GeoGitAppendCursor extends Cursor<Feature> {

    GeoGitDataset dataset;
    GeoGitTransaction tx;

    Feature curr;

    GeoGitAppendCursor(GeoGitDataset dataset, GeoGitTransaction tx) {
        super(Mode.APPEND);
        this.dataset = dataset;
        this.tx = tx;
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        curr = new BasicFeature(null, (List) null, dataset.schema());
        return curr;
    }

    @Override
    protected void doWrite() throws IOException {
        dataset.insert(curr, tx);
    }

    @Override
    public void close() throws IOException {

    }
}
