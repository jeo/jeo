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

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.geotools.GT;
import org.jeo.geotools.GTFeature;

public class GeoGitAppendCursor extends Cursor<Feature> {

    GeoGitDataset dataset;
    GeoGitTransaction tx;

    GTFeature curr;
    SimpleFeatureBuilder featureBuilder;

    GeoGitAppendCursor(GeoGitDataset dataset, GeoGitTransaction tx) {
        super(Mode.APPEND);
        this.dataset = dataset;
        this.tx = tx;

        featureBuilder = new SimpleFeatureBuilder(dataset.featureType());
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        curr = GT.feature(featureBuilder.buildFeature(null), dataset.schema());
        return curr;
    }

    @Override
    protected void doWrite() throws IOException {
        dataset.insert(curr.getFeature(), tx);
    }

    @Override
    public void close() throws IOException {

    }
}
