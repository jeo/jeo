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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.geogit.api.GeogitTransaction;
import org.geogit.api.plumbing.DiffIndex;
import org.geogit.api.plumbing.diff.DiffEntry;
import org.geogit.api.porcelain.AddOp;
import org.geogit.api.porcelain.CommitOp;
import org.geogit.api.porcelain.NothingToCommitException;
import org.jeo.data.Transaction;
import org.jeo.util.Convert;
import org.jeo.util.Optional;

import com.google.common.base.Preconditions;

public class GeoGitTransaction implements Transaction {

    static final String AUTHOR = "author";
    static final String MESSAGE = "message";

    GeogitTransaction ggtx;
    GeoGitDataset dataset;
    Map<String,Object> opts;

    GeoGitTransaction(GeogitTransaction ggtx, GeoGitDataset dataset, Map<String,Object> opts) {
        this.ggtx = ggtx;
        this.dataset = dataset;
        this.opts = opts != null ? opts : Collections.EMPTY_MAP;
    }

    @Override
    public void commit() throws IOException {
        Preconditions.checkState(ggtx != null);

        try {
            ggtx.command(AddOp.class).call();

            CommitOp commitOp = ggtx.command(CommitOp.class);

            Optional<String> message = Convert.toString(opts.get(MESSAGE));
            commitOp.setMessage(message.has() ? message.get() : autoMessage());

            Optional<String> author = Convert.toString(opts.get(AUTHOR));
            if (author.has()) {
                commitOp.setAuthor(author.get(), null);
                commitOp.setCommitter(author.get(), null);
            }

            /*if (timestamp != null) {
                commitOp.setAuthorTimestamp(timestamp.getTime());
                commitOp.setCommitterTimestamp(timestamp.getTime());
            }*/

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
        Iterator<DiffEntry> indexDiffs = ggtx.command(DiffIndex.class)
          .setFilter(Arrays.asList(dataset.getName())).call();
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
