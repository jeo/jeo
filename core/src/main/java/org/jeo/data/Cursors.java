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
package org.jeo.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.vector.Feature;
import org.jeo.vector.FeatureWrapper;
import org.jeo.vector.Features;
import org.jeo.filter.Filter;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Schema;
import org.jeo.vector.SchemaBuilder;

/**
 * Utility class for {@link Cursor} objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Cursors {

    /**
     * Returns the number of results in the cursor.
     */
    public static <T> int size(Cursor<T> cursor) throws IOException {
        try {
            int count = 0;
            while(cursor.hasNext()) {
                cursor.next();
                count++;
            }
            return count;
        }
        finally {
            cursor.close();
        }
    }

    /**
     * Returns the aggregated spatial extent of results in the cursor.
     */
    public static <T> Envelope extent(Cursor<T> cursor) throws IOException {
        try {
            Envelope extent = new Envelope();
            for (T obj : cursor) {
                Envelope e = envelope(obj);
                if (!e.isNull()) {
                    extent.expandToInclude(e);
                }
            }
            return extent;
        }
        finally {
            cursor.close();
        }
    }

    /**
     * Returns the first element of a cursor, returning <code>null</code> if the cursor has no 
     * more objects.
     * <p>
     * This method closed the cursor after attempting to obtain a feature therefore intended to be 
     * be used only in cases where this method is the only code accessing the cursor.
     * </p>
     * 
     * @param cursor The cursor.
     * 
     * @return The first available object, or <code>null</code> if no available.
     * 
     */
    public static <T> T first(Cursor<T> cursor) throws IOException {
        try {
            if (cursor.hasNext()) {
                return cursor.next();
            }

            return null;
        }
        finally {
            cursor.close();
        }
    }

    /**
     * Returns an {@link Iterator} for a cursor object.
     * <p>
     * This method should be typically used by cursor implementors implementing the {@link Iterable} 
     * interface.
     * </p>
     */
    public static <T> Iterator<T> iterator(final Cursor<T> c) {
        return new Iterator<T>() {
            boolean closed = false;

            @Override
            public boolean hasNext() {
                if (closed) {
                    return false;
                }

                try {
                    boolean hasNext = c.hasNext();
                    if (!hasNext) {
                        //close the cursor
                        c.close();
                    }

                    return hasNext;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public T next() {
                try {
                    return c.next();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns a typed empty cursor object.
     */
    public static <T> Cursor<T> empty(Class<T> clazz) {
        return new Cursor<T>() {
            @Override
            public boolean hasNext() throws IOException {
                return false;
            }
            @Override
            public T next() throws IOException {
                return null;
            }
            @Override
            public void close() throws IOException {
            }

            @Override
            public Iterator<T> iterator() {
                return Cursors.iterator(this);
            }
        };
    }

    /**
     * Returns a cursor containing a single object.
     */
    public static <T> Cursor<T> single(T obj) {
        return new SingleCursor<T>(obj);
    }

    private static class SingleCursor<T> extends Cursor<T> {

        T obj;

        SingleCursor(T obj) {
            this.obj = obj;
        }

        @Override
        public boolean hasNext() throws IOException {
            return obj != null;
        }

        @Override
        public T next() throws IOException {
            try {
                return obj;
            }
            finally {
                obj = null;
            }
        }

        @Override
        public void close() throws IOException {
        }
    }

    /**
     * Creates a cursor from an existing collection.
     */
    public static <T> Cursor<T> create(Collection<T> collection) {
        return create(collection.iterator());
    }

    /**
     * Creates a cursor from an existing iterator.
     */
    public static <T> Cursor<T> create(Iterator<T> it) {
        return new IteratorCursor<T>(it);
    }

    private static class IteratorCursor<T> extends Cursor<T> {
        Iterator<T> it;

        IteratorCursor(Iterator<T> it) {
            this.it = it;
        }

        @Override
        public Iterator<T> iterator() {
            return it;
        }

        @Override
        public boolean hasNext() throws IOException {
            return it.hasNext();
        }

        @Override
        public T next() throws IOException {
            return it.next();
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static class CursorWrapper<T> extends Cursor<T> {
        protected Cursor<T> delegate;

        CursorWrapper(Cursor<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() throws IOException {
            return delegate.hasNext();
        }

        @Override
        public T next() throws IOException {
            return delegate.next();
        }

        @Override
        public Cursor<T> write() throws IOException {
            return delegate.write();
        }

        @Override
        public Cursor<T> remove() throws IOException {
            return delegate.remove();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    /**
     * Wraps a cursor limiting the number of objects returned to a fixed size.
     * 
     * @param cursor The original cursor.
     * @param limit The maximum number of objects to return.
     * 
     * @return The wrapped cursor.
     */
    public static <T> Cursor<T> limit(Cursor<T> cursor, Integer limit) {
        return new LimitCursor<T>(cursor, limit);
    }

    private static class LimitCursor<T> extends CursorWrapper<T> {

        Integer limit;
        Integer count;

        LimitCursor(Cursor<T> delegate, Integer limit) {
            super(delegate);

            if (limit == null) {
                throw new NullPointerException("limit must not be null");
            }

            this.limit = limit;
            this.count = 0;
        }

        @Override
        public boolean hasNext() throws IOException {
            if (count < limit) {
                return delegate.hasNext();
            }
            return false;
        }

        @Override
        public T next() throws IOException {
            count++;
            return delegate.next();
        }
    }

    /**
     * Wraps a cursor skipping a specified number of objects. 
     * 
     * @param cursor The original cursor.
     * @param limit The number of objects of the original cursor to skip before returning objects.
     * 
     * @return The wrapped cursor.
     */
    public static <T> Cursor<T> offset(Cursor<T> cursor, Integer offset) {
        return new OffsetCursor<T>(cursor, offset);
    }

    private static class OffsetCursor<T> extends CursorWrapper<T> {

        Integer offset;
        
        OffsetCursor(Cursor<T> delegate, Integer offset) {
            super(delegate);

            if (offset == null) {
                throw new NullPointerException("limit must not be null");
            }

            this.offset = offset;
        }

        @Override
        public boolean hasNext() throws IOException {
            if (offset != null) {
                for (int i = 0; i < offset && delegate.hasNext(); i++) {
                    delegate.next();
                }
                offset = null;
            }
            return delegate.hasNext();
        }
    }

    /**
     * Wraps a cursor reprojecting objects to a specified coordinate reference system.
     * <p>
     * This method determines the source crs from objects in the underlying cursor. Use 
     * {@link #reproject(Cursor, CoordinateReferenceSystem, CoordinateReferenceSystem)} to 
     * explicitly specify the source crs.  
     * </p>
     * @param cursor The original cursor.
     * @param crs The crs to reproject to.
     * 
     * @return The wrapped cursor.
     */
    public static Cursor<Feature> reproject(Cursor<Feature> cursor, CoordinateReferenceSystem crs) {
        return reproject(cursor, null, crs);
    }

    /**
     * Wraps a cursor reprojecting objects between two specified coordinate reference systems.
     *
     * @param cursor The original cursor.
     * @param from The source crs.
     * @param to The destination crs.
     * 
     * @return The wrapped cursor.
     */
    public static Cursor<Feature> reproject(Cursor<Feature> cursor, CoordinateReferenceSystem from, 
        CoordinateReferenceSystem to) {

        return from != null ? 
            new TransformCursor(cursor, from, to) : new ReprojectCursor(cursor, to);
    }

    private static class ReprojectCursor extends CursorWrapper<Feature> {

        Map<String, CoordinateTransform> transforms;
        CoordinateReferenceSystem target;

        ReprojectCursor(Cursor<Feature> delegate, CoordinateReferenceSystem target) {
            super(delegate);
            if (delegate.getMode() != READ) {
                throw new IllegalArgumentException(
                    "Reproject cursor can only be applied to read only cursor");
            }

            this.target = target;
            transforms = new HashMap<String, CoordinateTransform>();
        }

        @Override
        public Feature next() throws IOException {
            Feature next = delegate.next();
            CoordinateReferenceSystem crs = next.crs();
            if (crs != null) {
                CoordinateTransform tx = transforms.get(crs.getName());
                if (tx == null) {
                    tx = Proj.transform(crs, target);
                    transforms.put(crs.getName(), tx);
                }
                return new TransformFeature(next, tx);
            }

            return next;
        }
    }

    private static class TransformCursor extends CursorWrapper<Feature> {

        CoordinateTransform tx;

        TransformCursor(Cursor<Feature> delegate, CoordinateReferenceSystem from, CoordinateReferenceSystem to) {
            super(delegate);
            tx = Proj.transform(from, to);
        }

        @Override
        public Feature next() throws IOException {
            return new TransformFeature(super.next(), tx);
        }
    }

    private static class TransformFeature extends FeatureWrapper {

        CoordinateTransform transform;

        TransformFeature(Feature delegate, CoordinateTransform transform) {
            super(delegate);
            this.transform = transform;
        }
    
        @Override
        public Geometry geometry() {
            Geometry g = super.geometry();
            return g != null ? reproject(g) : null;
        }

        @Override
        public Object get(String key) {
            Object obj = super.get(key);
            if (obj instanceof Geometry) {
                obj = reproject((Geometry)obj);
            }
            return obj;
        }

        public List<Object> list() {
            List<Object> l = new ArrayList<Object>(delegate.list());
            for (int i = 0; i < l.size(); i++) {
                Object obj = l.get(i);
                if (obj instanceof Geometry) {
                    l.set(i, reproject((Geometry) obj));
                }
            }
            return l;
        }

        public Map<String,Object> map() {
            LinkedHashMap<String,Object> m = new LinkedHashMap<String,Object>(delegate.map());
            for (Map.Entry<String, Object> e : m.entrySet()) {
                Object obj = e.getValue();
                if (obj instanceof Geometry) {
                    e.setValue(reproject((Geometry)obj));
                }
            }
            return m;
        }

        Geometry reproject(Geometry g) {
            return Proj.transform(g, transform);
        }
    }

    /**
     * Wraps a cursor returning objects that intersect the specified bounding box.
     *
     * @param cursor The original cursor.
     * @param bbox The bounding box filter.
     * 
     * @return The wrapped cursor.
     */
    public static Cursor<Feature> intersects(Cursor<Feature> cursor, Envelope bbox) {
        return new IntersectCursor(cursor, bbox);
    }

    private static class IntersectCursor extends CursorWrapper<Feature> {

        Envelope bbox;
        Feature next;

        IntersectCursor(Cursor<Feature> delegate, Envelope bbox) {
            super(delegate);
            this.bbox = bbox;
        }
    
        @Override
        public boolean hasNext() throws IOException {
            while(delegate.hasNext() && next == null) {
                Feature obj = delegate.next();
                if (intersects(obj)) {
                    next = obj;
                }
            }
            return next != null;
        }

        @Override
        public Feature next() throws IOException {
            Feature obj = next;
            next = null;
            return obj;
        }

        boolean intersects(Feature obj) {
            return envelope(obj).intersects(bbox);
        }
    }

    /**
     * Wraps a cursor returning objects that pass an attribute filter.
     *
     * @param cursor The original cursor.
     * @param filter The attribute filter.
     * 
     * @return The wrapped cursor.
     */
    public static <T> Cursor<T> filter(Cursor<T> cursor, Filter filter) {
        return new FilterCursor<T>(cursor, filter);
    }

    private static class FilterCursor<T> extends CursorWrapper<T> {

        Filter filter;
        T next;

        FilterCursor(Cursor<T> delegate, Filter filter) {
            super(delegate);
            this.filter = filter;
        }

        @Override
        public boolean hasNext() throws IOException {
            while(delegate.hasNext() && next == null) {
                T obj = delegate.next();
                if (filter.apply(obj)) {
                    next = obj;
                }
            }
            return next != null;
        }

        @Override
        public T next() throws IOException {
            T obj = next;
            next = null;
            return obj;
        }
    
    }

    /**
     * Transforms non geometry collection objects from the specified cursor to the appropriate 
     * geometry collection.
     */
    public static Cursor<Feature> multify(Cursor<Feature> cursor) {
        return new MultifyingCursor(cursor);
    }

    static class MultifyingCursor extends CursorWrapper<Feature> {

        GeometryFactory gfac;

        MultifyingCursor(Cursor<Feature> delegate) {
            super(delegate);
            gfac = new GeometryFactory();
        }
    
        @Override
        public Feature next() throws IOException {
            return Features.multify(super.next());
        }
    }

    static Envelope envelope(Object obj) {
        Geometry g = null;
        if (obj instanceof Geometry) {
            g = (Geometry) obj;
        }
        else if (obj instanceof Feature) {
            g = ((Feature)obj).geometry();
        }
        if (g != null) {
            return g.getEnvelopeInternal();
        }

        Envelope e = new Envelope();
        e.setToNull();
        return e;
    }

    static class SelectFieldsCursor extends CursorWrapper<Feature> {
        private final String[] fields;
        private final Map<String, Object> values;
        private Schema schema;

        public SelectFieldsCursor(Cursor<Feature> delegate, Set<String> fields) {
            super(delegate);
            this.fields = fields.toArray(new String[0]);
            this.values = new HashMap<String, Object>(fields.size());
        }

        @Override
        public Feature next() throws IOException {
            Feature next = super.next();
            if (next != null) {
                if (schema == null ) {
                    schema = schema(next);
                }

                // values is copied by BasicFeature, so we reuse it
                for (int i = 0; i < fields.length; i++) {
                    values.put(fields[i], next.get(fields[i]));
                }
                // this may not be the most efficient approach compared
                // to wrapping the feature
                next = new BasicFeature(next.getId(), values, schema);
            }
            return next;
        }

        public Schema schema(Feature original) {
            Schema s = null;
            // if schemaless, don't use any existing derived schema as it will
            // now look invalid due to removed features
            // otherwise, derive a new one
            if (!original.isSchemaless()) {
                s = SchemaBuilder.selectFields(original.schema(), Arrays.asList(fields));
            }
            return s;
        }
    }

    /**
     * Wraps a cursor returning Features that contain only the specified fields.
     * @see Features.selectFields
     *
     * @param cursor The original cursor.
     * @param fields The fields to include.
     * 
     * @return The wrapped cursor.
     */
    public static Cursor<Feature> selectFields(Cursor<Feature> cursor, Collection<String> fields) {
        return new SelectFieldsCursor(cursor, new HashSet<String>(fields));
    }
}

