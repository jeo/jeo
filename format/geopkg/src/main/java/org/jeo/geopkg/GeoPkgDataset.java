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

import java.io.IOException;
import java.util.Map;

import org.jeo.data.Dataset;
import org.jeo.data.Driver;
import org.jeo.proj.Proj;
import org.jeo.util.Key;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class GeoPkgDataset<T extends Entry> implements Dataset {

    T entry;
    GeoPkgWorkspace geopkg;

    protected GeoPkgDataset(T entry, GeoPkgWorkspace geopkg) {
        this.entry = entry;
        this.geopkg = geopkg;
    }

    @Override
    public Driver<?> getDriver() {
        return geopkg.getDriver();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return geopkg.getDriverOptions();
    }

    @Override
    public String getName() {
        return entry.getTableName();
    }

    @Override
    public String getTitle() {
        return entry.getIdentifier();
    }

    @Override
    public String getDescription() {
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
    public Envelope bounds() throws IOException {
        return entry.getBounds();
    }

    @Override
    public void close() {
    }

}
