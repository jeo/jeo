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
package io.jeo.postgis;

import io.jeo.vector.Field;
import io.jeo.sql.FilterSQLEncoder;

import com.vividsolutions.jts.geom.Geometry;

public class PostGISFilterEncoder extends FilterSQLEncoder {

    PostGISDataset dataset;

    public PostGISFilterEncoder(PostGISDataset dataset) {
        setPrimaryKey(dataset.getTable().primaryKey());
        setDbTypes(dataset.pg.getDbTypes());
        setSchema(dataset.schema());
    }

    @Override
    protected int srid(Geometry geo, Object obj) {
        if (geo.getSRID() == 0 && obj instanceof Field) {
            Field fld = (Field) obj;
            Integer srid = fld.property("srid", Integer.class);
            if (srid != null) {
                return srid;
            }
        }
        return super.srid(geo, obj);
    }
}
