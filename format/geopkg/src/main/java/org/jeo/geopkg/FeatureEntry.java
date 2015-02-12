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

import org.jeo.vector.Schema;
import org.jeo.geom.Geom;
import org.jeo.sql.PrimaryKey;

/**
 * Feature entry in a geopackage.
 * <p>
 * This class corresponds to the "geometry_columns" table.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class FeatureEntry extends Entry {

    Geom.Type geometryType;
    String geometryColumn;
    boolean z, m;
    
    Schema schema;
    PrimaryKey primaryKey;

    public FeatureEntry() {
        setDataType(DataType.Feature);
    }

    public String getGeometryColumn() {
        return geometryColumn;
    }

    public void setGeometryColumn(String geometryColumn) {
        this.geometryColumn = geometryColumn;
    }

    public Geom.Type getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(Geom.Type geometryType) {
        this.geometryType = geometryType;
    }

    public boolean hasZ() {
        return z;
    }

    public void setZ(boolean z) {
        this.z = z;
    }

    public boolean hasM() {
        return m;
    }

    public void setM(boolean m) {
        this.m = m;
    }

    Schema getSchema() {
        return schema;
    }

    void setSchema(Schema schema) {
        this.schema = schema;
    }

    PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    void init(FeatureEntry e) {
        super.init(e);
        setGeometryColumn(e.getGeometryColumn());
        setGeometryType(e.getGeometryType());
        setZ(e.hasZ());
        setM(e.hasM());
    }

    @Override
    FeatureEntry copy() {
        FeatureEntry e = new FeatureEntry();
        e.init(this);
        return e;
    }
}
