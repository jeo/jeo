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
import java.io.Reader;

import io.jeo.json.parser.ParseException;
import io.jeo.vector.Feature;
import io.jeo.geojson.parser.FeatureCollectionHandler;
import io.jeo.geojson.parser.RootHandler;
import io.jeo.json.parser.JSONParser;
import io.jeo.vector.FeatureCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoJSONCursor extends FeatureCursor {

    static Logger LOG = LoggerFactory.getLogger(GeoJSONCursor.class);

    Reader input;
    JSONParser parser;
    RootHandler handler;

    Feature next;

    GeoJSONCursor(Reader input) {
        this.input = input;
        this.parser = new JSONParser();
        this.handler = new RootHandler(new FeatureCollectionHandler());
    }

    @Override
    public boolean hasNext() throws IOException {
        while (next == null && !handler.isFinished()) {
            try {
                parser.parse(input, handler, true);
                Object obj = handler.getValue();
                if (obj instanceof Feature) {
                    next = (Feature) obj;
                }
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
        return next != null;
    }

    @Override
    public Feature next() throws IOException {
        try {
            return next;
        }
        finally {
            next = null;
        }
    }

    @Override
    public void close() throws IOException {
        if (input != null) {
            input.close();
            input = null;
        }
    }
}
