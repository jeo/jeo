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
package io.jeo.geopkg;

import java.io.IOException;

import io.jeo.geom.Bounds;
import io.jeo.geom.Geom;
import io.jeo.sql.DbTypes;

/**
 * GeoPackage DB abstraction layer.
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public abstract class Backend extends io.jeo.sql.Backend {

    protected final DbTypes dbTypes = new GeoPkgTypes();

    final FeatureEntry createFeatureEntry(Results rs) throws IOException {
        FeatureEntry e = new FeatureEntry();

        initEntry(e, rs);
        e.setGeometryColumn(rs.getString(10));
        e.setGeometryType(Geom.Type.from(rs.getString(11)));
        e.setZ((rs.getBoolean(12)));
        e.setM((rs.getBoolean(13)));
        return e;
    }

    /**
     * Sets common attributes of an entry.
     */
    final void initEntry(Entry e, Results rs) throws IOException {
        e.setTableName(rs.getString(0));
        e.setIdentifier(rs.getString(2));
        e.setDescription(rs.getString(3));
        e.setLastChange(rs.getString(4));
        e.setBounds(new Bounds(rs.getDouble(5), rs.getDouble(7), rs.getDouble(6), rs.getDouble(8)));
        e.setSrid(rs.getInt(9));
    }
}
