/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.geobuf;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.vividsolutions.jts.geom.Geometry;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureAppendCursor;
import io.jeo.vector.MapFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeobufAppendCursor extends FeatureAppendCursor {

    static Logger LOG = LoggerFactory.getLogger(GeobufAppendCursor.class);

    GeobufWriter gbw;
    Feature next;

    public GeobufAppendCursor(GeobufDataset data) throws IOException {
        gbw = data.writer();
    }

    @Override
    public Feature next() throws IOException {
        return next = new MapFeature(null, new LinkedHashMap<String,Object>()) {
            @Override
            public Feature put(Geometry g) {
                //hack
                return put("geometry", g);
            }
        };
    }

    @Override
    public GeobufAppendCursor write() throws IOException {
        gbw.append(next);
        return this;
    }

    @Override
    public void close() {
        if (gbw != null) {
            try {
                gbw.write().close();
            } catch (IOException e) {
                LOG.debug("Error closing geobuf writer", e);
            }
        }
        gbw = null;
    }
}
