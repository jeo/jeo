/* Copyright 2014 The jeo project. All rights reserved.
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
package io.jeo.ogr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.jeo.vector.FileVectorDriver;
import io.jeo.vector.VectorDriver;
import io.jeo.vector.Schema;
import io.jeo.util.Messages;

public abstract class OGRDriver<T> extends FileVectorDriver<T> {

    @Override
    public boolean isEnabled(Messages messages) {
        return new OGR().isEnabled(messages);
    }

    @Override
    public final List<String> aliases() {
        List<String> aliases = new ArrayList<>();
        aliases.addAll(getAliases());
        aliases.add(getOGRDriverName());
        return aliases;
    }

    protected Collection<? extends String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String family() {
        return "gdal";
    }

    @Override
    protected boolean canCreate(File file, Map<?, Object> opts, Messages msgs) {
        return false;
    }

    @Override
    protected T create(File file, Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean canOpen(File file, Map<?, Object> opts, Messages msgs) {
        Map mopts = new LinkedHashMap(opts);
        mopts.put(OGR.DRIVER, getOGRDriverName());

        return new OGR().canOpen(mopts, msgs);
    }

    @Override
    protected T open(File file, Map<?, Object> opts) throws IOException {
        return open(new OGR().open(opts));
    }

    protected abstract T open(OGRWorkspace workspace) throws IOException;

    protected abstract String getOGRDriverName();

    @Override
    public boolean supports(VectorDriver.Capability cap) {
        return OGR.CAPABILITIES.contains(cap);
    }
}
