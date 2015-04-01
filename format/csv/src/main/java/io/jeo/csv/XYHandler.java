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
package io.jeo.csv;

import java.io.IOException;
import java.util.Locale;

import io.jeo.geom.GeomBuilder;
import io.jeo.util.Convert;

import com.csvreader.CsvReader;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Handles CSV file with lat/lon columns.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class XYHandler extends CSVHandler {
    CSVOpts opts;
    GeomBuilder gb;

    XYHandler(CSVOpts opts) {
        this.opts = opts;
        this.gb = new GeomBuilder();
    }

    @Override
    public void header(CsvReader r) throws IOException {
        if (opts.getX() == null) {
            Integer x = null, y = null;
            for (int i = 0; i < r.getHeaderCount(); i++) {
                String s = r.getHeader(i);
                if (s.equalsIgnoreCase(opts.getXcol())) {
                    x = i;
                }
                if (s.equalsIgnoreCase(opts.getYcol())) {
                    y = i;
                }
            }

            if (x == null || y == null) {
                throw new IllegalStateException(String.format(Locale.ROOT,
                    "Unable to determine x, y columns from %s, %s", opts.getXcol(), opts.getYcol()));
            }

            opts.xy(x, y);
        }
    }

    @Override
    public Geometry geom(CsvReader r) throws IOException {
        Number x = Convert.toNumber(r.get(opts.getX())).get();
        Number y = Convert.toNumber(r.get(opts.getY())).get();
        return gb.point(x.doubleValue(), y.doubleValue()).toPoint();
    }
}
