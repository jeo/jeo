package org.jeo.csv;

import static org.jeo.csv.CSV.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.util.Key;
import org.jeo.util.Pair;

public class CSVOpts {

    Character delim = DELIM.getDefault();
    boolean header = HEADER.getDefault();

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
