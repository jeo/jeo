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
package io.jeo.ogr;

import java.io.IOException;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import io.jeo.vector.Feature;
import io.jeo.vector.FeatureCursor;
import io.jeo.vector.Schema;
import io.jeo.util.Pair;

public class OGRCursor extends FeatureCursor {

    Layer layer;
    DataSource dataSource;
    Schema schema;
    OGRDataset dataset;

    org.gdal.ogr.Feature next;
    Feature curr;
    boolean complete;

    public OGRCursor(Layer layer, DataSource dataSource, OGRDataset dataset) throws IOException {
        this.layer = layer;
        this.dataSource = dataSource;
        this.dataset = dataset;
        this.schema = dataset.schema();

        layer.ResetReading();
        complete = false;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (complete) {
            return false;
        }

        if (curr != null) {
            return true;
        }

        next = layer.GetNextFeature();
        if (next == null) {
            complete = true;
            return false;
        }

        curr = new OGRFeature(next, schema);
        return true;
    }

    @Override
    public Feature next() throws IOException {
        try {
            return curr;
        }
        finally {
            curr = null;
        }
    }

    @Override
    public void close() throws IOException {
        dataset.close(Pair.of(layer,dataSource));
    }
}
