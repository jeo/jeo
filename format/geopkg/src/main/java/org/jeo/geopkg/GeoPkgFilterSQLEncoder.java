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
package org.jeo.geopkg;

import org.jeo.filter.Spatial;
import org.jeo.sql.FilterSQLEncoder;

public class GeoPkgFilterSQLEncoder extends FilterSQLEncoder {

    @Override
    public Object visit(Spatial<?> spatial, Object obj) {
        abort(spatial, "Spatial filters unsupported");
        return null;
    }
}
