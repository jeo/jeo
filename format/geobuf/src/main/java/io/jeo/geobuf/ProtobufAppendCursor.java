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
package io.jeo.protobuf;

import java.io.IOException;
import java.util.List;

import io.jeo.vector.BasicFeature;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Schema;

public class ProtobufAppendCursor extends FeatureCursor {

    ProtobufWriter pbw;

    Schema schema;
    Feature next;

    public ProtobufAppendCursor(ProtobufDataset data) throws IOException {
        super(Mode.APPEND);

        pbw = data.writer();
        schema = data.schema();

        pbw.schema(schema);
    }

    @Override
    public boolean hasNext() throws IOException {
        return true;
    }

    @Override
    public Feature next() throws IOException {
        return next = new BasicFeature(null, (List) null, schema);
        /*return next = new MapFeature(null, new HashMap<String, Object>()) {
            @Override
            public void put(Geometry g) {
                //hack
                put("geometry", g);
            }
        };*/
    }

    @Override
    protected void doWrite() throws IOException {
        pbw.feature(next);
    }

    @Override
    public void close() throws IOException {
        if (pbw != null) {
            pbw.close();
        }
        pbw = null;
    }
}
