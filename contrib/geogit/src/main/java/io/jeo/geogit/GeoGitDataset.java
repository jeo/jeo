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
import java.util.Map;

import org.geogit.api.Bounded;
import org.geogit.api.GeoGIT;
import org.geogit.api.GeogitTransaction;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.api.RevTree;
import org.geogit.api.plumbing.LsTreeOp;
import org.geogit.api.plumbing.LsTreeOp.Strategy;
import org.geogit.api.plumbing.RevObjectParse;
import org.geogit.api.plumbing.TransactionBegin;
import org.geogit.api.porcelain.CheckoutOp;
import org.geogit.repository.WorkingTree;
import io.jeo.data.Cursor.Mode;
import io.jeo.data.Transaction;
import io.jeo.data.Transactional;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.VectorDataset;
import io.jeo.vector.Schema;
import io.jeo.util.Key;
import io.jeo.util.Pair;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorQueryPlan;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.vividsolutions.jts.geom.Envelope;

public class GeoGitDataset implements VectorDataset, Transactional {

    Pair<NodeRef,RevCommit> ref;
    GeoGitWorkspace geogit;
    Schema schema;

    public GeoGitDataset(Pair<NodeRef,RevCommit> ref, Schema schema, GeoGitWorkspace geogit) {
        this.ref = ref;
        this.schema = schema;
        this.geogit = geogit;
    }

    @Override
    public GeoGit driver() {
        return geogit.driver();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return geogit.driverOptions();
    }

    public GeoGIT getGeoGIT() {
        return geogit.getGeoGIT();
    }
    @Override
    public String name() {
        return schema.name();
    }

    @Override
    public Schema schema() throws IOException {
        return schema;
    }

    @Override
    public CoordinateReferenceSystem crs() {
        return schema.crs();
    }

    @Override
    public long count(VectorQuery q) throws IOException {
        if (q.isAll()) {
            return q.adjustCount(countAll());
        }

        return cursor(q).count();
    }

    long countAll() {
        return tree().size();
    }

    @Override
    public Envelope bounds() throws IOException {
        Envelope bounds = new Envelope();
        getRef().expand(bounds);
        return bounds;
    }

    @Override
    public FeatureCursor cursor(VectorQuery q) throws IOException {
        //require a transaction for non read only 
        if (q.mode() != Mode.READ && q.transaction() == null) {
            throw new IllegalArgumentException("Writable cursor requires a transaction");
        }

        Transaction tx =  (Transaction) q.transaction();

        if (q.mode() == Mode.APPEND) {
            return new GeoGitAppendCursor(this, tx);
        }

        LsTreeOp ls = geogit.getGeoGIT().command(LsTreeOp.class)
            .setStrategy(Strategy.FEATURES_ONLY).setReference(getRef().path());

        VectorQueryPlan qp = new VectorQueryPlan(q);
        
        final Envelope bbox = q.bounds();
        if (!Envelopes.isNull(bbox)) {
            qp.bounded();
            ls.setBoundsFilter(new Predicate<Bounded>() {
                @Override
                public boolean apply(Bounded input) {
                    return input.intersects(bbox);
                }
            });
        }

        return qp.apply(new GeoGitCursor(q.mode(), ls.call(), this, tx));
    }

    public GeoGitTransaction transaction(Map<String,Object> options) {
        GeogitTransaction ggtx = geogit.getGeoGIT().command(TransactionBegin.class).call();
        ggtx.command(CheckoutOp.class).setSource(geogit.branch()).call();

        return new GeoGitTransaction(ggtx, this);
    }

    @Override
    public void close() {
    }

    NodeRef getRef() {
        return ref.first;
    }

    RevCommit getRevision() {
        return ref.second;
    }

    RevTree tree() {
        ObjectId id = getRef().objectId();
        Optional<RevTree> tree = 
            geogit.getGeoGIT().command(RevObjectParse.class).setObjectId(id).call(RevTree.class);
        return tree.get();
    }

    SimpleFeatureType featureType() {
        return geogit.featureType(getRef());
    }

    void insert(SimpleFeature feature, Transaction tx) {
        workingTree(tx).insert(getRef().path(), feature);
    }

    void delete(String fid, Transaction tx) {
        workingTree(tx).delete(getRef().path(), fid);
    }

    WorkingTree workingTree(Transaction tx) {
        GeoGitTransaction gtx = tx instanceof GeoGitTransaction ? (GeoGitTransaction) tx : null; 
        return gtx != null ? gtx.ggtx.getWorkingTree() : 
            geogit.getGeoGIT().getRepository().getWorkingTree();
    }
}
