package org.jeo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeo.filter.Filter;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class Query {

    /** logger */
    static Logger LOGGER = LoggerFactory.getLogger(Query.class);

    /**
     * Query option for filtering results returned. 
     */
    public static final Option<Filter> FILTER = new Option<Filter>("filter");
    
    /**
     * Query option for limiting the number of results returned. 
     */
    public static final Option<Integer> LIMIT = new Option<Integer>("limit");

    /**
     * Query option for skipping over number of results. 
     */
    public static final Option<Integer> OFFSET = new Option<Integer>("offset");

    /**
     * Query option for sorting results.
     */
    public static final Option<List<Sort>> SORT = new Option<List<Sort>>("sort");

    /**
     * Query option for re-projecting results.
     */
    public static final Option<CoordinateReferenceSystem> REPROJECT = 
        new Option<CoordinateReferenceSystem>("reproject");

    /**
     * Query option for simplifying geometry of results.
     */
    public static final Option<Double> SIMPLIFY = new Option<Double>("simplify");

    /**
     * Properties to include in query.
     */
    List<String> properties = new ArrayList<String>();

    /**
     * Spatial bounds of the query.
     */
    Envelope bounds;

    /**
     * Query options.
     */
    Map<String,Object> options = new HashMap<String,Object>();

    public Query() {
    }

    public List<String> getProperties() {
        return properties;
    }

    public Envelope getBounds() {
        return bounds;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public Query properties(String... properties) {
        return properties(Arrays.asList(properties));
    }

    public Query properties(List<String> properties) {
        properties.clear();
        properties.addAll(properties);
        return this;
    }

    public Query filter(Filter filter) {
        set(FILTER, filter);
        return this;
    }

    public Query filter(String cql) {
        try {
            set(FILTER, CQL.parse(cql));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Query bounds(Envelope bounds) {
        this.bounds = bounds;
        return this;
    }

    public Query limit(Integer limit) {
        set(LIMIT, limit);
        return this;
    }

    public Query offset(Integer offset) {
        set(OFFSET, offset);
        return this;
    }

    public Query sort(String... sort) {
        List<Sort> list = new ArrayList<Sort>();
        for (String s : sort) {
            list.add(new Sort(s));
        }
        set(SORT, list);
        return this;
    }

    public Query reproject(String srs) {
        CoordinateReferenceSystem crs = Proj.crs(srs);
        if (crs == null) {
            throw new IllegalArgumentException("Unknown crs: " + srs);
        }
        return reproject(crs);
    }

    public Query reproject(CoordinateReferenceSystem crs) {
        set(REPROJECT, crs);
        return this;
    }

    public Query simplify(Double tolerance) {
        set(SIMPLIFY, tolerance);
        return this;
    }

    public <T> void set(Option<T> opt, T value) {
        options.put(opt.key, value);
    }

    public <T> T get(Option<T> opt) {
        return (T) options.get(opt.key);
    }

    public <T> T consume(Option<T> opt, T def) {
        if (options.containsKey(opt.key)) {
            return (T) options.remove(opt.key);
        }
        return null;
    }

    public boolean isAll() {
        return options.isEmpty() && Geom.isNull(bounds);
    }

    public <T> Cursor<T> apply(Cursor<T> cursor) {
        Filter filter = consume(FILTER, null);
        if (filter != null) {
            cursor = Cursors.filter(cursor, filter);
        }

        Integer offset = consume(OFFSET, null);
        if (offset != null) {
            cursor = Cursors.offset(cursor, offset);
        }

        Integer limit = consume(LIMIT, null);
        if (limit != null) {
            cursor = Cursors.limit(cursor, limit);
        }

        CoordinateReferenceSystem crs = consume(REPROJECT, null);
        if (crs != null) {
            cursor = Cursors.reproject(cursor, crs);
        }

        if (!options.isEmpty()) {
            for (Map.Entry<String, Object> e : options.entrySet()) {
                LOGGER.debug(String.format("Query option %s: %s ignored", e.getKey(), e.getValue()));
            }
        }
        return cursor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bounds == null) ? 0 : bounds.hashCode());
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        result = prime * result
                + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Query other = (Query) obj;
        if (bounds == null) {
            if (other.bounds != null)
                return false;
        } else if (!bounds.equals(other.bounds))
            return false;
        if (options == null) {
            if (other.options != null)
                return false;
        } else if (!options.equals(other.options))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }

    
}
