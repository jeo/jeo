package org.jeo.data;

import org.jeo.filter.Filter;
import org.jeo.geom.Envelopes;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Explains how a query is handled by a format driver.
 * <p>
 * This class is typically only used by format implementors. As a {@link Query} object is processed
 * the query plan is updated at aspects of the query such as bounds, filtering, limit/offset, etc..
 * are handled natively. Finally {@link #apply(Cursor)} should be called to augment a cursor with 
 * wrappers that handle the parts of the query that could not be handled natively.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class QueryPlan {

    Query q;

    boolean bounded;
    boolean filtered;
    boolean offsetted;
    boolean limited;
    boolean reprojected;
    boolean simplified;
    boolean sorted;

    public QueryPlan(Query q) {
        this.q = q;
    }

    /**
     * Whether {@link Query#getBounds()} was handled natively.
     */
    public boolean isBounded() {
        return bounded;
    }

    /**
     * Marks {@link Query#getBounds()} as being handled natively.
     */
    public void bounded() {
        this.bounded = true;
    }

    /**
     * Whether {@link Query#getFilter()} was handled natively.
     */
    public boolean isFiltered() {
        return filtered;
    }

    /**
     * Marks {@link Query#getFilter()} as being handled natively.
     */
    public void filtered() {
        filtered = false;
    }

    /**
     * Whether {@link Query#getSort()} was handled natively.
     */
    public boolean isSorted() {
        return sorted;
    }

    /**
     * Marks {@link Query#getSort()} as being handled natively.
     */
    public void sorted() {
        sorted = true;
    }

    /**
     * Whether {@link Query#getOffset()} was handled natively.
     */
    public boolean isOffsetted() {
        return offsetted;
    }

    /**
     * Marks {@link Query#getOffset()} as being handled natively.
     */
    public void offsetted() {
        offsetted = true;
    }

    /**
     * Whether {@link Query#getLimit()} was handled natively.
     */
    public boolean isLimited() {
        return limited;
    }

    /**
     * Marks {@link Query#getLimit()} as being handled natively.
     */
    public void limited() {
        limited = true;
    }

    /**
     * Whether {@link Query#getReproject()} was handled natively.
     */
    public boolean isReprojected() {
        return reprojected;
    }

    /**
     * Marks {@link Query#getReproject()} as being handled natively.
     */
    public void reprojected() {
        reprojected = true;
    }

    /**
     * Whether {@link Query#getSimplify()} was handled natively.
     */
    public boolean isSimplified() {
        return simplified;
    }

    /**
     * Marks {@link Query#getSimplify()} as being handled natively.
     */
    public void simplified() {
        simplified = true;
    }

    /**
     * Augments the specified cursor with wrappers that handle the parts of the query that could
     * not be processed natively.
     * <p>
     * For example, if a format is unable to process {@link Query#getFilter()} objects natively 
     * then {@link #isFiltered()} should return <tt>false</tt> and this method should wrap the 
     * cursor with {@link Cursors#filter(Cursor, Filter)}.
     * </p>
     * @param cursor Cursor to augment.
     * 
     * @return The augmented cursor.
     */
    public <T> Cursor<T> apply(Cursor<T> cursor) {

        Envelope bounds = q.getBounds();
        if (!isBounded() && !Envelopes.isNull(bounds)) {
            cursor = Cursors.intersects(cursor, bounds);
        }

        Filter filter = q.getFilter();
        if (!isFiltered() && !Filter.isFalseOrNull(filter)) {
            cursor = Cursors.filter(cursor, filter);
        }

        Integer offset = q.getOffset();
        if (!isOffsetted() && offset != null) {
            cursor = Cursors.offset(cursor, offset);
        }

        Integer limit = q.getLimit();
        if (!isLimited() && limit != null) {
            cursor = Cursors.limit(cursor, limit);
        }

        Pair<CoordinateReferenceSystem,CoordinateReferenceSystem> reproj = q.getReproject();
        if (!isReprojected() && reproj != null) {
            cursor = Cursors.reproject(cursor, reproj.first(), reproj.second());
        }

        //TODO: sorting
        return cursor;
    }

}
