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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.jeo.util.Key;
import io.jeo.util.Pair;

import static io.jeo.csv.CSV.*;

public class CSVOpts {

    Character delim = DELIM.def();
    boolean header = HEADER.def();

    String xcol, ycol, wktcol;
    Integer x, y, wkt;

    List<Pair<Object,Class<?>>> mappings = new ArrayList<Pair<Object,Class<?>>>();

    public static CSVOpts fromMap(Map<?, Object> map) {
        CSVOpts csvOpts = new CSVOpts();
        csvOpts.delimiter(DELIM.get(map)).header(HEADER.get(map));
        
        Object x = X.get(map);
        if (x instanceof Integer) {
            csvOpts.xy((Integer)x, (Integer)Y.get(map));
        }
        else {
            csvOpts.xy(x.toString(), Y.get(map).toString());
        }

        return csvOpts;
    }

    public Character getDelimiter() {
        return delim;
    }

    public CSVOpts delimiter(Character delim) {
        this.delim = delim;
        return this;
    }

    public boolean hasHeader() {
        return header;
    }

    public CSVOpts header(boolean header) {
        this.header = header;
        return this;
    }

    public CSVOpts xy(String x, String y) {
        this.xcol = x;
        this.ycol = y;
        return this;
    }

    public CSVOpts xy(Integer x, Integer y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public CSVOpts wkt(String wkt) {
        this.wktcol = wkt;
        return this;
    }

    public CSVOpts wkt(Integer wkt) {
        this.wkt = wkt;
        return this;
    }

    public CSVOpts map(Integer col, Class<?> type) {
        mappings.add(new Pair<Object,Class<?>>(col, type));
        return this;
    }

    public CSVOpts map(String col, Class<?> type) {
        mappings.add(new Pair<Object,Class<?>>(col, type));
        return this;
    }

    public Map<Key<?>,Object> toMap() {
        Map<Key<?>, Object> map = new LinkedHashMap<Key<?>, Object>();
        map.put(DELIM, delim);
        map.put(HEADER, header);
        map.put(X, x != null ? x : xcol);
        map.put(Y, y != null ? y : ycol);
        return map;
    }

    Integer getX() {
        return x;
    }

    String getXcol() {
        return xcol;
    }

    Integer getY() {
        return y;
    }

    String getYcol() {
        return ycol;
    }

    String getWktCol() {
        return wktcol;
    }

    Integer getWkt() {
        return wkt;
    }

    CSVHandler handler() {
        //sanity checks

        if ((wktcol != null && !header) || (xcol != null && ycol != null && !header)) { 
            throw new IllegalArgumentException("specifying column names requires a header");
        }

        if (x == null && y == null && wkt == null && xcol == null && ycol == null && wktcol == null) {
            throw new IllegalArgumentException("Must specify x/y columns or wkt column");
        }

        return (wkt == null && wktcol == null) ? new XYHandler(this) : new WKTHandler(this); 
    }
}
