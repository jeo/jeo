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
package org.jeo.lucene;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import org.jeo.util.Key;
import org.jeo.util.Messages;
import org.jeo.vector.FileVectorDriver;
import org.jeo.vector.Schema;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Lucene driver.
 */
public class Lucene extends FileVectorDriver<LuceneDataset> {

    /**
     * List of spatial fields in the lucene index.
     */
    public static Key<String> SPATIAL_FIELDS = new Key<>("spatial_fields", String.class, null, true);

    /**
     * Spatial context.
     */
    public static Key<JtsSpatialContext> SPATIAL_CONTEXT =
        new Key<>("spatial_context", JtsSpatialContext.class, JtsSpatialContext.GEO);

    public static LuceneDataset open(LuceneOpts opts) throws IOException {
        return new LuceneDataset(opts);
    }

    @Override
    public List<Key<?>> keys() {
        return Arrays.asList(FILE, SPATIAL_FIELDS, SPATIAL_CONTEXT);
    }

    @Override
    protected LuceneDataset open(File file, Map<?, Object> opts) throws IOException {
        return null;
    }

    @Override
    public String name() {
        return "Lucene";
    }

    @Override
    public Class<LuceneDataset> type() {
        return LuceneDataset.class;
    }

    @Override
    protected boolean canOpen(File file, Map<?, Object> opts, Messages msgs) {
        if (!super.canOpen(file, opts, msgs)) {
            return false;
        }

        if (!file.isDirectory()) {
            Messages.of(msgs).report(file.getPath() + " is not a directory");
            return false;
        }

        return true;
    }

    @Override
    protected boolean canCreate(File file, Map<?, Object> opts, Messages msgs) {
        if (file.exists()) {
            // ensure it's a directory
            if (!file.isDirectory()) {
                Messages.of(msgs).report(file.getPath() + " exists and is not a directory");
                return false;
            }

            // ensure it's an empty directory
            if (file.list().length != 0) {
                Messages.of(msgs).report(file.getPath() + " is not empty");
                return false;
            }
        }

        return true;
    }

    @Override
    protected LuceneDataset create(File file, Map<?, Object> opts, Schema schema) throws IOException {
        return null;
    }

    @Override
    public boolean supports(Capability cap) {
        return false;
    }
}
