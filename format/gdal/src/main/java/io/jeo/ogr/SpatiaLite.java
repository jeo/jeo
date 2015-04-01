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
import java.util.Collections;
import java.util.Map;

/**
 * SpatiaLite driver.
 * <p>
 * Usage:
 * <pre><code>
 * Workspace db = SpatiaLite.open('usa.db');
 * </code></pre>
 * </p>
 * </p>
 * <p>
 * This driver is implemented on top of the OGR library.
 * </p>
 * @author Justin Deoliveira, Boundless
 */
public class SpatiaLite extends OGRDriver<OGRWorkspace> {

    public static OGRWorkspace open(File file) throws IOException {
        return new SpatiaLite().open((Map)Collections.singletonMap(FILE, file));
    }

    @Override
    public String name() {
        return "SpatiaLite";
    }
    
    @Override
    public Class<OGRWorkspace> type() {
        return OGRWorkspace.class;
    }
    
    @Override
    protected OGRWorkspace open(OGRWorkspace workspace) throws IOException {
        return workspace;
    }
    
    @Override
    protected String getOGRDriverName() {
        return "SQLite";
    }

}
