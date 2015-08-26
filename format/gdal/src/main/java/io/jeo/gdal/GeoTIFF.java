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
package io.jeo.gdal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * GeoTIFF driver.
 * <p>
 * Usage:
 * <pre><code>
 * Dataset dem = GeoTIFF.open('dem.tif');
 * </code></pre>
 * <p>
 * This driver is implemented on top of the GDAL library.
 * </p>
 * @author Justin Deoliveira, Boundless
 */
public class GeoTIFF extends GDAL {

    public static GDALDataset open(Path path) throws IOException {
        return new GeoTIFF().open(path.toFile(), null);
    }

    public GeoTIFF() {
        super();
    }

    @Override
    public String name() {
        return "GeoTIFF";
    }

    @Override
    public String getGDALDriverName() {
        return "GTiff";
    }

    @Override
    public List<String> aliases() {
        List<String> aliases = new ArrayList<>();
        aliases.addAll(super.aliases());
        aliases.add("tif");
        aliases.add("tiff");
        return aliases;
    }
}
