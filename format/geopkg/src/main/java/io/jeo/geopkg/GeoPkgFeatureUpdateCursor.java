package io.jeo.geopkg;

import io.jeo.data.Transaction;
import io.jeo.sql.Backend.Session;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.FeatureWriteCursor;

import java.io.IOException;

public class GeoPkgFeatureUpdateCursor extends FeatureWriteCursor {

    FeatureCursor cursor;
    Session session;
    Transaction tx;
    FeatureEntry entry;
    GeoPkgWorkspace workspace;

    Feature feature;

    public GeoPkgFeatureUpdateCursor(FeatureCursor cursor, Session session, Transaction tx, FeatureEntry entry, GeoPkgWorkspace workspace)
        throws IOException {

        this.cursor = cursor;
        this.session = session;
        this.tx = tx;
        this.entry = entry;
        this.workspace = workspace;

        if (tx == Transaction.NULL) {
            // without a transaction, performance is miserable so start one
            session.beginTransaction();
        }
    }
    @Override
    public boolean hasNext() throws IOException {
        return cursor.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        return feature = cursor.next();
    }

    @Override
    public GeoPkgFeatureUpdateCursor write() throws IOException {
        workspace.update(entry, feature, session);
        return this;
    }

    @Override
    public GeoPkgFeatureUpdateCursor remove() throws IOException {
        workspace.delete(entry, feature, session);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (tx == Transaction.NULL) {
            if (session != null) {
                // close the transaction we created
                session.endTransaction(true);

                // lack of a transaction passed in means we need to close the session as well
                session.close();
            }
        }
    }
}
