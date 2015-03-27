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
package org.jeo.csv;

import java.io.IOException;
import java.util.Locale;

import com.csvreader.CsvReader;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WKTHandler extends CSVHandler {

    CSVOpts opts;

    WKTHandler(CSVOpts opts) {
        this.opts = opts;
    }

    @Override
    public void header(CsvReader r) throws IOException {
        if (opts.getWkt() == null) {
            Integer wkt = null;
            for (int i = 0; i < r.getHeaderCount(); i++) {
                String col = r.getHeader(i);
                if (col.equalsIgnoreCase(opts.getWktCol())) {
                    wkt = i;
                    break;
                }
            }

            if (wkt == null) {
                throw new IllegalStateException(String.format(Locale.ROOT,
                    "Unable to determine wkt column from %s", opts.getWktCol()));
            }

            opts.wkt(wkt);
        }
    }

    @Override
    public Geometry geom(CsvReader r) throws IOException {
        String wkt = r.get(opts.getWkt());
        if ("".equals(wkt)) {
            return null;
        }

        try {
            return new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new IOException(e); 
        }
    }
}
