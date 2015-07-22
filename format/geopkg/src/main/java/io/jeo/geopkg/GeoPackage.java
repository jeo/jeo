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
package io.jeo.geopkg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * GeoPackage driver that uses the JDBCBackend.
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class GeoPackage extends GeoPkgBaseDriver {

    public static GeoPkgWorkspace open(Path path) throws IOException {
        File file = path.toFile();
        return new GeoPackage().open(file, (Map) Collections.singletonMap(FILE, file));
    }

    @Override
    protected Backend backend(GeoPkgOpts gpkgOpts) throws IOException {
        return new JDBCBackend(gpkgOpts);
    }

}
