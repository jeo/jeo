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
package io.jeo.csv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import io.jeo.vector.FileVectorDriver;
import io.jeo.vector.VectorDriver;
import io.jeo.vector.Schema;
import io.jeo.util.Key;
import io.jeo.util.Messages;

public class CSV extends FileVectorDriver<CSVDataset> {

    public static final Key<Character> DELIM = new Key<Character>("delim", Character.class, ',');

    public static final Key<Boolean> HEADER = new Key<Boolean>("header", Boolean.class, true);

    public static final Key<Object> X = new Key<Object>("x", Object.class, "x");

    public static final Key<Object> Y = new Key<Object>("y", Object.class, "y");

    public static CSVDataset open(File file, CSVOpts csvOpts) throws IOException {
        return new CSVDataset(file, csvOpts);
    }

    @Override
    public String name() {
        return "CSV";
    }

    @Override
    public List<Key<?>> keys() {
        return (List) Arrays.asList(FILE, DELIM, HEADER, X, Y);
    }

    @Override
    public Class<CSVDataset> type() {
        return CSVDataset.class;
    }

    @Override
    protected boolean canOpen(File file, Map<?,Object> opts, Messages msgs) {
        if (!super.canOpen(file, opts, msgs)) {
            return false;
        }

        if (!file.isFile()) {
            msgs.report(file.getPath() + " is not a file");
            return false;
        }
        return true;
    }

    @Override
    public CSVDataset open(File file, Map<?,Object> opts) throws IOException {
        return new CSVDataset(file, CSVOpts.fromMap(opts));
    }

    @Override
    protected boolean canCreate(File file, Map<?, Object> opts, Messages msgs) {
        //TODO: implement
        Messages.of(msgs).report("Creation unsupported");
        return false;
    }

    @Override
    protected CSVDataset create(File file, Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    static final EnumSet<Capability> CAPABILITIES = EnumSet.noneOf(Capability.class);

    @Override
    public boolean supports(VectorDriver.Capability cap) {
        return CAPABILITIES.contains(cap);
    }
}
