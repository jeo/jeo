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
package io.jeo.geojson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.jeo.vector.FileVectorDriver;
import io.jeo.vector.Schema;

import static io.jeo.util.Util.set;

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
    public static GeoJSONDataset open(Path path) {
        return new GeoJSONDataset(path.toFile());
    }

    @Override
    public String name() {
        return "GeoJSON";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("json");
    }
    
    @Override
    public Class<GeoJSONDataset> type() {
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

    static final Set<Capability> CAPABILITIES = set(APPEND);

    @Override
    public Set<Capability> capabilities() {
        return CAPABILITIES;
    }
}
