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
package io.jeo.geotools;

import org.geotools.data.DataUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shapefile extends GTVectorDriver {

    static final String CLASS = "org.geotools.data.shapefile.ShapefileDataStoreFactory";

    public static GTWorkspace open(File file) throws IOException {
        Map<String,Object> opts = new HashMap<>();
        opts.put("url", DataUtilities.fileToURL(file));

        return new Shapefile().open(opts);
    }

    public Shapefile() {
        super(CLASS);
    }

    @Override
    public String name() {
        return "Shapefile";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("shp");
    }

}
