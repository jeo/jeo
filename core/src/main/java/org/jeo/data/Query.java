package org.jeo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeo.filter.Filter;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;
import org.jeo.geom.Envelopes;
import org.jeo.proj.Proj;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Describes a query against a {@link VectorData} dataset. 
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class Query {

    /** logger */
    static Logger LOGGER = LoggerFactory.getLogger(Query.class);

    /**
     * Fields to include in query.
     */
    List<String> fields = new ArrayList<String>();

    /**
     * Spatial bounds of the query.
     */
    Envelope bounds;

    /**
     * Filter of the query
     */
    Filter filter;

    /**
     * Limit / offset
     */
    Integer limit, offset;

    /** 
     * sorting
     */
    List<Sort> sort;

    /**
     * reprojection
     */
    Pair<CoordinateReferenceSystem,CoordinateReferenceSystem> reproject;

    /**
     * simplification
     */
    Double simplify;

    /**
     * Transaction associated with the query
     */
    Transaction transaction;

    /**
     * Cursor mode
     */
    Cursor.Mode mode = Cursor.READ;

    /**
     * New query instance.
     */
    public Query() {
    }

    /**
     * List of Feature properties to query, an empty list means all properties.
     *
     */
    public List<String> getFields() {
        return fields;
    }

    /**
     * Bounds constraints on the query, may be <code>null</code> meaning no bounds constraint.
     */
    public Envelope getBounds() {
        return bounds;
    }

    /**
     * Constraint on the query, may be <code>null</code> meaning no constraint.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Limit on the number of features to return from the query, <code>null</code> meaning no limit.
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     *  Offset into query result set from which to start returning features, <code>null</code> 
     *  meaning no offset.
     */
    public Integer getOffset() {
        return offset;
    }

    /**
     * Coordinate reference systems to reproject feature results between, <code>null</code> 
     * meaning no reprojection should occur.
     * <p>
     * The first element in the pair may be <code>null</code> to signify that the dataset crs 
     * (if available) should be used
     * </p>
     */
    public Pair<CoordinateReferenceSystem, CoordinateReferenceSystem> getReproject() {
        return reproject;
    }

    /**
     * Simplification tolerance to apply to feature geometries, <code>null</code> meaning no 
     * simplification.
     */
    public Double getSimplify() {
        return simplify;
    }

    /**
     * Sort criteria for the query, <code>null</code> meaning no sorting.
     */
    public List<Sort> getSort() {
        return sort;
    }

    /**
     * The mode of cursor to return when handling this query.
     */
    public Cursor.Mode getMode() {
        return mode;
    }

    /**
     * Transaction of the query, may be <code>null</code>.
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * Sets the field list of the query.
     * 
     * @return This object.
     */
    public Query fields(String... properties) {
        return fields(Arrays.asList(properties));
    }

    /**
     * Sets the field list of the query.
     * 
     * @return This object.
     */
    public Query fields(List<String> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
        return this;
    }

    /**
     * Sets the filter of the query from a CQL string.
     * 
     * @return This object.
     */
    public Query filter(String cql) {
        try {
            return filter(CQL.parse(cql));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the filter of the query.
     * 
     * @return This object.
     */
    public Query filter(Filter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets the bounds constraint of the query.
     * 
     * @return This object.
     */
    public Query bounds(Envelope bounds) {
        this.bounds = bounds;
        return this;
    }

    /**
     * Sets the limit on the number of results to return from the query.
     * 
     * @return This object.
     */
    public Query limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Sets the number of results to skip over from the query.
     * 
     * @return This object.
     */
    public Query offset(Integer offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Sets the properties to sort results by.
     *
     * @return This object.
     */
    public Query sort(String... sort) {
        List<Sort> list = new ArrayList<Sort>();
        for (String s : sort) {
            list.add(new Sort(s));
        }
        this.sort = list;
        return this;
    }

    /**
     * Sets the srs to re-project query results to. 
     * 
     * @return This object.
     */
    public Query reproject(String srs) {
        return reproject(null, srs);
    }

    /**
     * Sets the srs to re-project query results to. 
     * 
     * @return This object.
     */
    public Query reproject(String from, String to) {
        CoordinateReferenceSystem src = from != null ? Proj.crs(from) : null;
        CoordinateReferenceSystem dst = to != null ? Proj.crs(to) : null;
         
        if (from != null && src == null) {
            throw new IllegalArgumentException("Unknown crs: " + from);
        }
        if (to == null) {
            throw new IllegalArgumentException("Unknown crs: " + to);
        }
        return reproject(src, dst);
    }

    /**
     * Sets the crs to re-project query results to. 
     * 
     * @return This object.
     */
    public Query reproject(CoordinateReferenceSystem crs) {
        return reproject(null, crs);
    }

    /**
     * Sets the source/target crs to re-project query results from/to. 
     * 
     * @return This object.
     */
    public Query reproject(CoordinateReferenceSystem from, CoordinateReferenceSystem to) {
        reproject = new Pair<CoordinateReferenceSystem,CoordinateReferenceSystem>(from, to);
        return this;
    }

    /**
     * Sets the tolerance with which to simplify geometry of query results.   
     * 
     * @return This object.
     */
    public Query simplify(Double tolerance) {
        simplify = tolerance;
        return this;
    }

    /**
     * Sets the query to update mode, specifying that any returned cursor should be in mode 
     * {@link Cursor#UPDATE}.
     * 
     * @return This object.
     */
    public Query update() {
        mode = Cursor.UPDATE;
        return this;
    }

    /**
     * Sets the query to append mode, specifying that any returned cursor should be in mode 
     * {@link Cursor#APPEND}.
     * 
     * @return This object.
     */
    public Query append() {
        mode = Cursor.APPEND;
        return this;
    }

    /**
     * Sets the transaction of the query.
     * 
     * @return This object.
     */
    public Query transaction(Transaction tx) {
        this.transaction = tx;
        return this;
    }

    /**
     * Determines if the query constrains results with a bounds constraint or filter.
     * 
     * @return True if no bounds or filter constraint is applied, otherwise false.
     */
    public boolean isAll() {
        return Envelopes.isNull(bounds) && !isFiltered();
    }

    /**
     * Determines if the query constrains results with or filter.
     */
    public boolean isFiltered() {
        return !Filter.isTrueOrNull(filter);
    }

    /**
     * Adjusts a raw count based on limit and offset of the query.
     * <p>
     * The adjusted count is equivalent to:
     * <pre>
     * min(max(0, count-offset), limit)
     * </pre>
     * </p>
     * @return The adjusted count.
     */
    public long adjustCount(long count) {
        if (offset != null) {
            count = Math.max(0, count - offset);
        }
        if (limit != null) {
            count = Math.min(count, limit);
        }

        return count;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bounds == null) ? 0 : bounds.hashCode());
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        result = prime * result + ((limit == null) ? 0 : limit.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((offset == null) ? 0 : offset.hashCode());
        result = prime * result
                + ((reproject == null) ? 0 : reproject.hashCode());
        result = prime * result
                + ((simplify == null) ? 0 : simplify.hashCode());
        result = prime * result + ((sort == null) ? 0 : sort.hashCode());
        result = prime * result
                + ((transaction == null) ? 0 : transaction.hashCode());
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
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        if (filter == null) {
            if (other.filter != null)
                return false;
        } else if (!filter.equals(other.filter))
            return false;
        if (limit == null) {
            if (other.limit != null)
                return false;
        } else if (!limit.equals(other.limit))
            return false;
        if (mode != other.mode)
            return false;
        if (offset == null) {
            if (other.offset != null)
                return false;
        } else if (!offset.equals(other.offset))
            return false;
        if (reproject == null) {
            if (other.reproject != null)
                return false;
        } else if (!reproject.equals(other.reproject))
            return false;
        if (simplify == null) {
            if (other.simplify != null)
                return false;
        } else if (!simplify.equals(other.simplify))
            return false;
        if (sort == null) {
            if (other.sort != null)
                return false;
        } else if (!sort.equals(other.sort))
            return false;
        if (transaction == null) {
            if (other.transaction != null)
                return false;
        } else if (!transaction.equals(other.transaction))
            return false;
        return true;
    }

    
}
