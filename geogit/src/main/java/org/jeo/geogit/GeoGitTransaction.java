package org.jeo.geogit;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.geogit.api.GeogitTransaction;
import org.geogit.api.plumbing.DiffIndex;
import org.geogit.api.plumbing.TransactionEnd;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.NothingToCommitException;
import org.jeo.data.Transaction;

import com.google.common.base.Preconditions;

public class GeoGitTransaction implements Transaction {

    GeogitTransaction ggtx;
    GeoGitDataset dataset;

    GeoGitTransaction(GeogitTransaction ggtx, GeoGitDataset dataset) {
        this.ggtx = ggtx;
        this.dataset = dataset;
    }

    @Override
    public void commit() throws IOException {
        commit(null, null, null);
    }

    public void commit(String message, String author, Date timestamp) {
        Preconditions.checkState(ggtx != null);

        try {
            ggtx.command(AddOp.class).call();

            CommitOp commitOp = ggtx.command(CommitOp.class);
            commitOp.setMessage(message != null ? message : autoMessage());

            if (author != null) {
                commitOp.setAuthor(author, null);
                commitOp.setCommitter(author, null);
            }
            if (timestamp != null) {
                commitOp.setAuthorTimestamp(timestamp.getTime());
                commitOp.setCommitterTimestamp(timestamp.getTime());
            }

            commitOp.call();
            ggtx.commit();

        } catch (NothingToCommitException nochanges) {
            // ok
        }
    }

    @Override
    public void rollback() throws IOException {
        Preconditions.checkState(ggtx != null);
        ggtx.abort();
        ggtx = null;
    }

    String autoMessage() {
        Iterator<DiffEntry> indexDiffs = ggtx.command(DiffIndex.class).setFilter(dataset.getName()).call();
        int added = 0, removed = 0, modified = 0;
        StringBuilder msg = new StringBuilder();
        while (indexDiffs.hasNext()) {
            DiffEntry entry = indexDiffs.next();
            switch (entry.changeType()) {
            case ADDED:
                added++;
                break;
            case MODIFIED:
                modified++;
                break;
            case REMOVED:
                removed++;
                break;
            }
            if ((added + removed + modified) < 10) {
                msg.append("\n ").append(entry.changeType().toString().toLowerCase()).append(" ")
                        .append(entry.newPath() == null ? entry.oldName() : entry.newPath());
            }
        }
        int count = added + removed + modified;
        if (count > 10) {
            msg.append("\n And ").append(count - 10).append(" more changes.");
        }
        StringBuilder title = new StringBuilder();
        if (added > 0) {
            title.append("added ").append(added);
        }
        if (modified > 0) {
            if (title.length() > 0) {
                title.append(", ");
            }
            title.append("modified ").append(modified);
        }
        if (removed > 0) {
            if (title.length() > 0) {
                title.append(", ");
            }
            title.append("removed ").append(removed);
        }
        if (count > 0) {
            title.append(" features via unversioned legacy client.\n");
        }
        msg.insert(0, title);
        return msg.toString();
    }
}
