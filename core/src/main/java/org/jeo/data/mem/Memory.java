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
package org.jeo.data.mem;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.jeo.raster.RasterDriver;
import org.jeo.vector.VectorDriver;
import org.jeo.vector.Schema;
import org.jeo.util.Key;
import org.jeo.util.Messages;

/**
 * Driver for in memory workspace objects.
 * <p>
 * Not meant to be used as a true format, mostly for testing purposes. The driver maintains a static
 * map of Workspace objects which are loadable by {@link #NAME}.
 * </p>
 * 
 * @author Justin Deoliveira, Boundless
 */
public class Memory implements VectorDriver<MemWorkspace>, RasterDriver<MemWorkspace> {

    /**
     * Name of the workspace, may be null to signify the default workspace. 
     */
    public static final Key<String> NAME = new Key<String>("name", String.class);

    static Map<String,MemWorkspace> WORKSPACES = new ConcurrentHashMap<String, MemWorkspace>();

    public static MemWorkspace open(String name) {
        return new Memory().open((Map) Collections.singletonMap(NAME, name));
    }

    @Override
    public boolean isEnabled(Messages messages) {
        return true;
    }

    @Override
    public String name() {
        return "Memory";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("mem");
    }

    @Override
    public Class<MemWorkspace> type() {
        return MemWorkspace.class;
    }

    @Override
    public List<Key<?>> keys() {
        return (List) Arrays.asList(NAME);
    }

    @Override
    public boolean canOpen(Map<?, Object> opts, Messages messages) {
        return NAME.in(opts);
    }

    @Override
    public MemWorkspace open(Map<?, Object> opts)  {
        String name = NAME.get(opts);
        name = name == null ? "" : name;

        MemWorkspace ws = WORKSPACES.get(name);
        if (ws != null) {
            return ws;
        }

        synchronized (this) {
            WORKSPACES.put(name, new MemWorkspace());
        }

        return WORKSPACES.get(name);
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        return canOpen(opts, msgs);
    }

    @Override
    public MemWorkspace create(Map<?, Object> opts, Schema schema) throws IOException {
        MemWorkspace ws = open(opts);
        ws.create(schema);
        return ws;
    }

    static final EnumSet<VectorDriver.Capability> VECTOR_CAPABILITIES = EnumSet.of(VectorDriver.Capability.BOUND);
    static final EnumSet<RasterDriver.Capability> RASTER_CAPABILITIES = EnumSet.of(RasterDriver.Capability.RESAMPLE);

    @Override
    public boolean supports(VectorDriver.Capability cap) {
       return VECTOR_CAPABILITIES.contains(cap);
    }

    @Override
    public boolean supports(RasterDriver.Capability cap) {
        return RASTER_CAPABILITIES.contains(cap);
    }
}
