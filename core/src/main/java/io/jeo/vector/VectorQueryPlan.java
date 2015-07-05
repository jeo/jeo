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
package io.jeo.vector;

import io.jeo.filter.Filters;
import io.jeo.util.Pair;
import io.jeo.filter.Filter;
import io.jeo.util.Predicate;
import io.jeo.geom.Envelopes;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import java.util.Set;

/**
 * Explains how a query is handled by a format driver.
 * <p>
 * This class is typically only used by format implementers. As a {@link VectorQuery} object is processed
 * the query plan is updated at aspects of the query such as bounds, filtering, limit/offset, etc..
 * are handled natively. Finally {@link #apply(FeatureCursor)} should be called to augment a cursor with
 * wrappers that handle the parts of the query that could not be handled natively.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class VectorQueryPlan {

    VectorQuery q;

    boolean bounded;
    Filter<Feature> filter;
    boolean offsetted;
    boolean limited;
    boolean reprojected;
    boolean simplified;
    boolean sorted;
    boolean fieldsSelected;

    public VectorQueryPlan(VectorQuery q) {
        this.q = q;
        filter = q.filter();
    }

    /**
     * Whether {@link VectorQuery#bounds()} was handled natively.
     */
    public boolean isBounded() {
        return bounded;
    }

    /**
     * Marks {@link VectorQuery#bounds()} as being handled natively.
     */
    public void bounded() {
        this.bounded = true;
    }

    /**
     * Whether {@link VectorQuery#filter()} was handled natively.
     */
    public boolean isFiltered() {
        return Filters.isTrueOrNull(filter);
    }

    /**
     * Marks {@link VectorQuery#filter()} as being handled natively.
     */
    public void filtered() {
        filter = Filters.all();
    }

    /**
     * Marks {@link VectorQuery#filter()} as being handled natively with a left-over part of the filter that could not
     * be handled.
     */
    public void filtered(Filter left) {
        filter = left;
    }

    /**
     * Whether {@link VectorQuery#sort()} was handled natively.
     */
    public boolean isSorted() {
        return sorted;
    }

    /**
     * Marks {@link VectorQuery#sort()} as being handled natively.
     */
    public void sorted() {
        sorted = true;
    }

    /**
     * Whether {@link VectorQuery#offset()} was handled natively.
     */
    public boolean isOffsetted() {
        return offsetted;
    }

    /**
     * Marks {@link VectorQuery#offset()} as being handled natively.
     */
    public void offsetted() {
        offsetted = true;
    }

    /**
     * Whether {@link VectorQuery#limit()} was handled natively.
     */
    public boolean isLimited() {
        return limited;
    }

    /**
     * Marks {@link VectorQuery#limit()} as being handled natively.
     */
    public void limited() {
        limited = true;
    }

    /**
     * Whether {@link VectorQuery#reproject()} was handled natively.
     */
    public boolean isReprojected() {
        return reprojected;
    }

    /**
     * Marks {@link VectorQuery#reproject()} as being handled natively.
     */
    public void reprojected() {
        reprojected = true;
    }

    /**
     * Whether {@link VectorQuery#simplify()} was handled natively.
     */
    public boolean isSimplified() {
        return simplified;
    }

    /**
     * Marks {@link VectorQuery#simplify()} as being handled natively.
     */
    public void simplified() {
        simplified = true;
    }

    /**
     * Whether {@link VectorQuery#fields()} was handled natively.
     */
    public boolean isFields() {
        return fieldsSelected;
    }

    /**
     * Marks {@link VectorQuery#fields()} as being handled natively.
     */
    public void fields() {
        this.fieldsSelected = true;
    }

    /**
     * Augments the specified cursor with wrappers that handle the parts of the query that could
     * not be processed natively.
     * <p>
     * For example, if a format is unable to process {@link VectorQuery#filter()} objects natively
     * then {@link #isFiltered()} should return <tt>false</tt> and this method should transform the
     * cursor with {@link FeatureCursor#filter(Predicate)}.
     * </p>
     * @param cursor Cursor to augment.
     * 
     * @return The augmented cursor.
     */
    public FeatureCursor apply(FeatureCursor cursor) {

        Envelope bounds = q.bounds();
        if (!isBounded() && !Envelopes.isNull(bounds)) {
            cursor = cursor.intersect(bounds, true);
        }

        if (!Filters.isTrueOrNull(filter)) {
            cursor = cursor.filter(filter);
        }

        Integer offset = q.offset();
        if (!isOffsetted() && offset != null) {
            cursor = cursor.skip(offset);
        }

        Integer limit = q.limit();
        if (!isLimited() && limit != null) {
            cursor = cursor.limit(limit);
        }

        Pair<CoordinateReferenceSystem,CoordinateReferenceSystem> reproj = q.reproject();
        if (!isReprojected() && reproj != null) {
            cursor = cursor.reproject(reproj.first, reproj.second);
        }

        Set<String> fields = q.fields();
        if (!isFields() && !fields.isEmpty()) {
            cursor = cursor.select(fields);
        }

        //TODO: sorting
        return cursor;
    }

}
