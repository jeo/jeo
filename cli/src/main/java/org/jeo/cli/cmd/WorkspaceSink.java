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
package org.jeo.cli.cmd;

import org.jeo.cli.ConsoleProgress;
import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.data.Drivers;
import org.jeo.data.Transaction;
import org.jeo.data.Transactional;
import org.jeo.data.Workspace;
import org.jeo.util.Disposer;
import org.jeo.util.Pair;
import org.jeo.util.Supplier;
import org.jeo.vector.Feature;
import org.jeo.vector.FeatureCursor;
import org.jeo.vector.Features;
import org.jeo.vector.Schema;
import org.jeo.vector.VectorDataset;
import org.jeo.vector.VectorQuery;

import java.io.IOException;
import java.net.URI;

import static java.lang.String.format;

/**
 * Vector sink that writes data into a new dataset of a workspace.
 */
public class WorkspaceSink implements VectorSink {
    Pair<URI, String> ref;

    public WorkspaceSink(Pair<URI,String> ref) {
        this.ref = ref;
    }

    @Override
    public void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException {
        try (Disposer disposer = new Disposer()) {
            Workspace dest = disposer.open(Drivers.open(ref.first, Workspace.class));
            if (dest == null) {
                throw new IllegalArgumentException("Unable to open workspace: " + ref.first);
            }

            // buffer the cursor to read the first feature
            cursor = cursor.buffer(1);

            Schema schema = cursor.first().orElseThrow(new Supplier<RuntimeException>() {
                @Override
                public RuntimeException get() {
                    return new IllegalArgumentException("No data to write");
                }
            }).schema();

            cursor.rewind();

            String dsName = ref.second;
            if (dsName != null) {
                // rename the schema
                schema = Schema.build(dsName).fields(schema.getFields()).schema();
            }

            dsName = schema.getName();
            if (disposer.open(dest.get(dsName)) != null) {
                throw new IllegalStateException(
                format("Data set %s already exists in workspace %s", dsName, ref.first));
            }

            try {
                VectorDataset dataset = disposer.open(dest.create(schema));
                copy(cursor, dataset, source, disposer, cli);
            }
            catch(UnsupportedOperationException e) {
                throw new IllegalStateException(format("Workspace %s does not support creating data sets", ref.first));
            }

        }
    }

    void copy(Cursor<Feature> cursor, VectorDataset dataset, VectorDataset source, Disposer disposer, JeoCLI cli)
            throws IOException {

        Transaction tx = dataset instanceof Transactional ? ((Transactional) dataset).transaction(null) : null;
        FeatureCursor target = disposer.open(dataset.cursor(new VectorQuery().append().transaction(tx)));

        ConsoleProgress progress = cli.progress(-1);
        if (source != null) {
            progress.init((int) source.count(new VectorQuery()));
        }

        for (Feature f : cursor) {
            progress.inc();

            Feature g = target.next();
            Features.copy(f, g);
            target.write();
        }
    }
}
