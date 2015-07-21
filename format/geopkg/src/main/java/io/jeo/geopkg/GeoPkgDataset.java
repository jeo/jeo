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

import java.io.IOException;
import java.util.Map;

import io.jeo.data.Dataset;
import io.jeo.data.Driver;
import io.jeo.geom.Bounds;
import io.jeo.proj.Proj;
import io.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

public class GeoPkgDataset<T extends Entry> implements Dataset {

    T entry;
    GeoPkgWorkspace geopkg;

    protected GeoPkgDataset(T entry, GeoPkgWorkspace geopkg) {
        this.entry = entry;
        this.geopkg = geopkg;
    }

    @Override
    public Driver<?> driver() {
        return geopkg.driver();
    }

    @Override
    public Map<Key<?>, Object> driverOptions() {
        return geopkg.driverOptions();
    }

    @Override
    public String name() {
        return entry.getTableName();
    }

    public String title() {
        return entry.getIdentifier();
    }

    public String description() {
        return entry.getDescription();
    }
    
    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        if (entry.getSrid() != null) {
            return Proj.crs(entry.getSrid());
        }
        return null;
    }
    
    @Override
    public Bounds bounds() throws IOException {
        return entry.getBounds();
    }

    @Override
    public void close() {
    }

}
