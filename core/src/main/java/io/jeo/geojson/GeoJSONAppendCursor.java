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
package io.jeo.geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import io.jeo.vector.FeatureCursor;
import io.jeo.vector.BasicFeature;
import io.jeo.vector.Feature;

import com.vividsolutions.jts.geom.Geometry;

public class GeoJSONAppendCursor extends FeatureCursor {

    GeoJSONWriter writer;
    Feature next;

    public GeoJSONAppendCursor(Writer out) throws IOException {
        super(Mode.APPEND);
        writer = new GeoJSONWriter(out);
        writer.featureCollection();
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }
    
    @Override
    public Feature next() throws IOException {
        return next = new BasicFeature(null, new HashMap<String, Object>()) {
            @Override
            public BasicFeature put(Geometry g) {
                //hack
                return put("geometry", g);
            }
        };
    }

    @Override
    protected void doWrite() throws IOException {
        writer.feature(next);
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.endFeatureCollection();
            writer.flush();
        }
        writer = null;
    }
}
