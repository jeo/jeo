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

import io.jeo.vector.Feature;
import io.jeo.vector.FeatureAppendCursor;

import com.vividsolutions.jts.geom.Geometry;
import io.jeo.vector.MapFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoJSONAppendCursor extends FeatureAppendCursor {

    static Logger LOG = LoggerFactory.getLogger(GeoJSONAppendCursor.class);

    GeoJSONWriter writer;
    Feature next;

    public GeoJSONAppendCursor(Writer out) throws IOException {
        writer = new GeoJSONWriter(out);
        writer.featureCollection();
    }

    @Override
    public Feature next() throws IOException {
        return next = new MapFeature(null, new HashMap<String, Object>()) {
            @Override
            public Feature put(Geometry g) {
                //hack
                return put("geometry", g);
            }
        };
    }

    @Override
    public GeoJSONAppendCursor write() throws IOException {
        writer.feature(next);
        return this;
    }

    @Override
    public void close() {
        if (writer != null) {
            try {
                writer.endFeatureCollection();
            } catch (IOException e) {
                throw new RuntimeException("Error closing feature collection object", e);
            }

            try {
                writer.flush();
            }
            catch(IOException e) {
                LOG.debug("Error flushing writer");
            }
        }
        writer = null;
    }
}
