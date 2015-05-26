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
import java.util.Iterator;

import io.jeo.geobuf.Geobuf.Data;
import io.jeo.geobuf.Geobuf.Data.FeatureCollection;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;

public class GeobufCursor extends FeatureCursor {

    Iterator<Data.Feature> it;
    GeobufReader reader;

    public GeobufCursor(FeatureCollection fcol, GeobufReader reader) {
        this.reader = reader;
        this.it = fcol.getFeaturesList().iterator();
    }

    @Override
    public boolean hasNext() throws IOException {
        return it.hasNext();
    }

    @Override
    public Feature next() throws IOException {
        return reader.decode(it.next());
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }
}
