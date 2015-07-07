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
package io.jeo.geopkg;

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
import io.jeo.util.Password;
import io.jeo.vector.VectorDriver.Capability;

import static io.jeo.vector.VectorDriver.Capability.FIELD;
import static io.jeo.vector.VectorDriver.Capability.FILTER;
import static io.jeo.vector.VectorDriver.Capability.LIMIT;
import static io.jeo.vector.VectorDriver.Capability.OFFSET;

/**
 * Support for a Geopackage Driver. An implementation only needs to provide
 * a {@link Backend} via the {@link #backend(GeoPkgOpts)} method
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public abstract class GeoPkgBaseDriver extends FileVectorDriver<GeoPkgWorkspace> {

    /**
     * User key, defaults to no user.
     */
    public static final Key<String> USER = new Key<String>("user", String.class);

    /**
     * Password key.
     */
    public static final Key<Password> PASSWD = new Key<Password>("passwd", Password.class);

    @Override
    public final GeoPkgWorkspace open(File file, Map<?, Object> opts) throws IOException {
        GeoPkgOpts gpkgOpts = GeoPkgOpts.fromMap(opts);
        return new GeoPkgWorkspace(backend(gpkgOpts), gpkgOpts);
    }

    /**
     * Create a new Backend using the provided {@link GeoPkgOpts}.
     * @param gpkgOpts non-null options
     * @return non-null Backend
     * @throws IOException if there is a problem creating the Backend
     */
    protected abstract Backend backend(GeoPkgOpts gpkgOpts) throws IOException;

    @Override
    public final List<Key<?>> keys() {
        return (List) Arrays.asList(FILE, USER, PASSWD);
    }

    @Override
    public final String name() {
        return "GeoPackage";
    }

    @Override
    public final List<String> aliases() {
        return Arrays.asList("gpkg", "geopkg");
    }

    @Override
    public final Class<GeoPkgWorkspace> type() {
        return GeoPkgWorkspace.class;
    }

    @Override
    protected final GeoPkgWorkspace create(File file, Map<?, Object> opts, Schema schema) throws IOException {
        GeoPkgWorkspace ws = open(file, opts);
        ws.create(schema);
        return ws;
    }

    static final EnumSet<Capability> CAPABILITIES = EnumSet.of(FILTER, LIMIT, OFFSET, FIELD);

    @Override
    public final boolean supports(Capability cap) {
        return CAPABILITIES.contains(cap);
    }

}
