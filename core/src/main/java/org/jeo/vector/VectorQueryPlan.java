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
package org.jeo.vector;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.filter.Filter;
import org.jeo.filter.Filters;
import org.jeo.geom.Envelopes;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import java.util.Set;

/**
 * Explains how a query is handled by a format driver.
 * <p>
 * This class is typically only used by format implementers. As a {@link VectorQuery} object is processed
 * the query plan is updated at aspects of the query such as bounds, filtering, limit/offset, etc..
 * are handled natively. Finally {@link #apply(org.jeo.data.Cursor)} should be called to augment a cursor with
 * wrappers that handle the parts of the query that could not be handled natively.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class VectorQueryPlan {

    VectorQuery q;

    boolean bounded;
    boolean filtered;
    boolean offsetted;
    boolean limited;
    boolean reprojected;
    boolean simplified;
    boolean sorted;
    boolean fieldsSelected;

    public VectorQueryPlan(VectorQuery q) {
        this.q = q;
    }

    /**
     * Whether {@link VectorQuery#getBounds()} was handled natively.
     */
    public boolean isBounded() {
        return bounded;
    }

    /**
     * Marks {@link VectorQuery#getBounds()} as being handled natively.
     */
    public void bounded() {
        this.bounded = true;
    }

    /**
     * Whether {@link VectorQuery#getFilter()} was handled natively.
     */
    public boolean isFiltered() {
        return filtered;
    }

    /**
     * Marks {@link VectorQuery#getFilter()} as being handled natively.
     */
    public void filtered() {
        filtered = true;
    }

    /**
     * Whether {@link VectorQuery#getSort()} was handled natively.
     */
    public boolean isSorted() {
        return sorted;
    }

    /**
     * Marks {@link VectorQuery#getSort()} as being handled natively.
     */
    public void sorted() {
        sorted = true;
    }

    /**
     * Whether {@link VectorQuery#getOffset()} was handled natively.
     */
    public boolean isOffsetted() {
        return offsetted;
    }

    /**
     * Marks {@link VectorQuery#getOffset()} as being handled natively.
     */
    public void offsetted() {
        offsetted = true;
    }

    /**
     * Whether {@link VectorQuery#getLimit()} was handled natively.
     */
    public boolean isLimited() {
        return limited;
    }

    /**
     * Marks {@link VectorQuery#getLimit()} as being handled natively.
     */
    public void limited() {
        limited = true;
    }

    /**
     * Whether {@link VectorQuery#getReproject()} was handled natively.
     */
    public boolean isReprojected() {
        return reprojected;
    }

    /**
     * Marks {@link VectorQuery#getReproject()} as being handled natively.
     */
    public void reprojected() {
        reprojected = true;
    }

    /**
     * Whether {@link VectorQuery#getSimplify()} was handled natively.
     */
    public boolean isSimplified() {
        return simplified;
    }

    /**
     * Marks {@link VectorQuery#getSimplify()} as being handled natively.
     */
    public void simplified() {
        simplified = true;
    }

    /**
     * Whether {@link VectorQuery#getFields()} was handled natively.
     */
    public boolean isFields() {
        return fieldsSelected;
    }

    /**
     * Marks {@link VectorQuery#getFields()} as being handled natively.
     */
    public void fields() {
        this.fieldsSelected = true;
    }

    /**
     * Augments the specified cursor with wrappers that handle the parts of the query that could
     * not be processed natively.
     * <p>
     * For example, if a format is unable to process {@link VectorQuery#getFilter()} objects natively
     * then {@link #isFiltered()} should return <tt>false</tt> and this method should wrap the 
     * cursor with {@link org.jeo.data.Cursors#filter(org.jeo.data.Cursor, Filter)}.
     * </p>
     * @param cursor Cursor to augment.
     * 
     * @return The augmented cursor.
     */
    public Cursor<Feature> apply(Cursor<Feature> cursor) {

        Envelope bounds = q.getBounds();
        if (!isBounded() && !Envelopes.isNull(bounds)) {
            cursor = Cursors.intersects(cursor, bounds);
        }

        Filter<Feature> filter = q.getFilter();
        if (!isFiltered() && !Filters.isFalseOrNull(filter)) {
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
            cursor = Cursors.reproject(cursor, reproj.first, reproj.second);
        }

        Set<String> fields = q.getFields();
        if (!isFields() && !fields.isEmpty()) {
            cursor = Cursors.selectFields(cursor, fields);
        }

        //TODO: sorting
        return cursor;
    }

}
