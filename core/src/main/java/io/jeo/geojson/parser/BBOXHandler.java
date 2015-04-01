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
package io.jeo.geojson.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.jeo.json.parser.ParseException;

import com.vividsolutions.jts.geom.Envelope;

public class BBOXHandler extends BaseHandler {

    List<Double> values = new ArrayList<Double>();

    @Override
    public boolean startArray() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        values.add(((Number)value).doubleValue());
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        if (values.size() < 4) {
            throw new IllegalStateException("expected 4 values for bbox");
        }

        Envelope bbox = new Envelope(values.get(0), values.get(2), values.get(1), values.get(3));
        node.setValue(bbox);

        pop();
        return true;
    }
}
