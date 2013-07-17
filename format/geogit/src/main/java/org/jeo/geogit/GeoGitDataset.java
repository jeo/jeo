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
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.data.Transactional;
import org.jeo.data.VectorData;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.geom.Envelopes;
import org.jeo.util.Key;
import org.jeo.util.Pair;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.vividsolutions.jts.geom.Envelope;

public class GeoGitDataset implements VectorData, Transactional {

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
    public Schema getSchema() throws IOException {
        return schema;
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        return schema.crs();
    }

    @Override
    public long count(Query q) throws IOException {
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
    public Cursor<Feature> cursor(Query q) throws IOException {
        //require a transaction for non read only 
        if (q.getMode() != Mode.READ && q.getTransaction() == null) {
            throw new IllegalArgumentException("Writable cursor requires a transaction");
        }

        GeoGitTransaction tx =  (GeoGitTransaction) q.getTransaction();

        if (q.getMode() == Mode.APPEND) {
            return new GeoGitAppendCursor(this, tx);
        }

        LsTreeOp ls = geogit.getGeoGIT().command(LsTreeOp.class)
            .setStrategy(Strategy.FEATURES_ONLY).setReference(getRef().path());

        QueryPlan qp = new QueryPlan(q);
        
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
        return ref.first();
    }

    RevCommit getRevision() {
        return ref.second();
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

    void insert(SimpleFeature feature, GeoGitTransaction tx) {
        workingTree(tx).insert(getRef().path(), feature);
    }

    void delete(String fid, GeoGitTransaction tx) {
        workingTree(tx).delete(getRef().path(), fid);
    }

    WorkingTree workingTree(GeoGitTransaction tx) {
        return tx != null ? tx.ggtx.getWorkingTree() : 
            geogit.getGeoGIT().getRepository().getWorkingTree();
    }
}
