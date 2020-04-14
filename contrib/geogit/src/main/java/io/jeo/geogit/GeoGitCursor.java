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
package io.jeo.geogit;

import java.io.IOException;
import java.util.Iterator;

import org.geogit.api.FeatureBuilder;
import org.geogit.api.NodeRef;
import org.geogit.api.RevFeature;
import org.geogit.api.plumbing.RevObjectParse;
import io.jeo.data.Transaction;
import io.jeo.vector.Feature;
import io.jeo.geotools.GT;
import io.jeo.geotools.GTFeature;
import io.jeo.vector.FeatureCursor;
import org.opengis.feature.simple.SimpleFeature;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class GeoGitCursor extends FeatureCursor {

    Iterator<NodeRef> nodeit;
    GeoGitDataset dataset;

    RevObjectParse parseOp;
    FeatureBuilder featureBuilder;

    GTFeature curr;
    Transaction tx;

    GeoGitCursor(Mode mode, Iterator<NodeRef> nodeit, GeoGitDataset dataset, Transaction tx) {
        super(mode);
        this.nodeit = nodeit;
        this.dataset = dataset;
        this.tx = tx;

        featureBuilder = new FeatureBuilder(dataset.featureType());
        parseOp = dataset.getGeoGIT().command(RevObjectParse.class);
    }

    @Override
    public boolean hasNext() throws IOException {
        return nodeit.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        NodeRef ref = nodeit.next();
        parseOp.setObjectId(ref.objectId());

        Optional<RevFeature> f = parseOp.call(RevFeature.class);
        Preconditions.checkState(f.isPresent());

        curr = GT.feature(
            (SimpleFeature)featureBuilder.build(ref.name(), f.get()), dataset.schema());
        return curr;
    }

    @Override
    protected void doWrite() throws IOException {
        Preconditions.checkNotNull(curr, "next() has not been called");
        dataset.insert(curr.getFeature(), tx);
    }

    @Override
    protected void doRemove() throws IOException {
        Preconditions.checkNotNull(curr, "next() has not been called");
        dataset.delete(curr.id(), tx);
    }

    @Override
    public void close() throws IOException {
    }
}
