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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Shapefile driver.
 * <p>
 * Usage:
 * <pre><code>
 * Dataset shp = Shapefile.open('states.shp');
 * </code></pre>
 * </p>
 * </p>
 * <p>
 * This driver is implemented on top of the OGR library.
 * </p>
 * @author Justin Deoliveira, Boundless
 */
public class Shapefile extends OGRDriver<OGRDataset> {

    public static OGRDataset open(File file) throws IOException {
        return new Shapefile().open((Map)Collections.singletonMap(FILE, file));
    }

    @Override
    public String name() {
        return "Shapefile";
    }

    @Override
    public Class<OGRDataset> type() {
        return OGRDataset.class;
    }

    @Override
    protected Collection<? extends String> getAliases() {
        return Arrays.asList("shp");
    }

    @Override
    protected String getOGRDriverName() {
        return "ESRI Shapefile";
    }

    @Override
    protected OGRDataset open(OGRWorkspace workspace) throws IOException {
        return workspace.get(0);
    }
}
