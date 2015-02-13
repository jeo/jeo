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
import org.jeo.data.Cursor;
import org.jeo.data.Cursor.Mode;
import org.jeo.data.Cursors;
import org.jeo.data.Transaction;
import org.jeo.data.Transactional;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.Feature;
import org.jeo.vector.Schema;
import org.jeo.geom.Envelopes;
import org.jeo.util.Key;
import org.jeo.util.Pair;
import org.jeo.vector.VectorQuery;
import org.jeo.vector.VectorQueryPlan;
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
    public GeoGit getDriver() {
        return geogit.getDriver();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return geogit.getDriverOptions();
    }

    public GeoGIT getGeoGIT() {
        return geogit.getGeoGIT();
    }
    @Override
    public String getName() {
        return schema.getName();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
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

        return Cursors.size(cursor(q));
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
    public Cursor<Feature> cursor(VectorQuery q) throws IOException {
        //require a transaction for non read only 
        if (q.getMode() != Mode.READ && q.getTransaction() == null) {
            throw new IllegalArgumentException("Writable cursor requires a transaction");
        }

        Transaction tx =  (Transaction) q.getTransaction();

        if (q.getMode() == Mode.APPEND) {
            return new GeoGitAppendCursor(this, tx);
        }

        LsTreeOp ls = geogit.getGeoGIT().command(LsTreeOp.class)
            .setStrategy(Strategy.FEATURES_ONLY).setReference(getRef().path());

        VectorQueryPlan qp = new VectorQueryPlan(q);
        
        final Envelope bbox = q.getBounds();
        if (!Envelopes.isNull(bbox)) {
            qp.bounded();
            ls.setBoundsFilter(new Predicate<Bounded>() {
                @Override
                public boolean apply(Bounded input) {
                    return input.intersects(bbox);
                }
            });
        }

        return qp.apply(new GeoGitCursor(q.getMode(), ls.call(), this, tx));
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
