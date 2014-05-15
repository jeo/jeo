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
package org.jeo.geojson;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileVectorDriver;
import org.jeo.data.VectorDriver;
import org.jeo.feature.Schema;

/**
 * GeoJSON format driver.
 * <p>
 * Usage:
 * <pre><code>
 * GeoJSON.open(new File("states.json"));
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoJSON extends FileVectorDriver<GeoJSONDataset> {

    /**
     * Opens a file containing encoded GeoJSON.
     */
    public static GeoJSONDataset open(File file) {
        return new GeoJSONDataset(file);
    }

    @Override
    public String getName() {
        return "GeoJSON";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("json");
    }
    
    @Override
    public Class<GeoJSONDataset> getType() {
        return GeoJSONDataset.class;
    }

    @Override
    public GeoJSONDataset open(File file, Map<?, Object> opts) throws IOException {
        return new GeoJSONDataset(file);
    }

    @Override
    protected GeoJSONDataset create(File file, Map<?, Object> opts, Schema schema) 
        throws IOException {
        return new GeoJSONDataset(file);
    }

    static final EnumSet<VectorDriver.Capability> CAPABILITIES = EnumSet.noneOf(VectorDriver.Capability.class);

    @Override
    public boolean supports(VectorDriver.Capability cap) {
        return CAPABILITIES.contains(cap);
    }
}
