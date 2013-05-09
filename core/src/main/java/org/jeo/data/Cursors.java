package org.jeo.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.feature.Feature;
import org.jeo.feature.FeatureWrapper;
import org.jeo.filter.Filter;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

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
        public void write() throws IOException {
            delegate.write();
        }

        @Override
        public void remove() throws IOException {
            delegate.remove();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

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

    public static <T> Cursor<T> reproject(Cursor<T> cursor, CoordinateReferenceSystem crs) {
        return reproject(cursor, null, crs);
    }
    
    public static <T> Cursor<T> reproject(Cursor<T> cursor, CoordinateReferenceSystem from, 
        CoordinateReferenceSystem to) {

        return from != null ? 
            new TransformCursor(cursor, from, to) : new ReprojectCursor(cursor, to);
    }

    private static class ReprojectCursor<T extends Feature> extends CursorWrapper<T> {

        Map<String, CoordinateTransform> transforms;
        CoordinateReferenceSystem target;

        ReprojectCursor(Cursor<T> delegate, CoordinateReferenceSystem target) {
            super(delegate);
            if (delegate.getMode() != READ) {
                throw new IllegalArgumentException(
                    "Reproject cursor can only be applied to read only cursor");
            }

            this.target = target;
            transforms = new HashMap<String, CoordinateTransform>();
        }

        @Override
        public T next() throws IOException {
            T next = delegate.next();
            CoordinateReferenceSystem crs = next.crs();
            if (crs != null) {
                CoordinateTransform tx = transforms.get(crs.getName());
                if (tx == null) {
                    tx = Proj.transform(crs, target);
                    transforms.put(crs.getName(), tx);
                }
                return (T) new TransformFeature(next, tx);
            }

            return next;
        }
    }

    private static class TransformCursor<T extends Feature> extends CursorWrapper<T> {

        CoordinateTransform tx;

        TransformCursor(Cursor<T> delegate, CoordinateReferenceSystem from, CoordinateReferenceSystem to) {
            super(delegate);
            tx = Proj.transform(from, to);
        }

        @Override
        public T next() throws IOException {
            return (T) new TransformFeature(super.next(), tx);
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
            return Proj.reproject(g, transform);
        }
    }

    public static <T> Cursor<T> intersects(Cursor<T> cursor, Envelope bbox) {
        return new IntersectCursor<T>(cursor, bbox);
    }

    private static class IntersectCursor<T> extends CursorWrapper<T> {

        Envelope bbox;
        T next;

        IntersectCursor(Cursor<T> delegate, Envelope bbox) {
            super(delegate);
            this.bbox = bbox;
        }
    
        @Override
        public boolean hasNext() throws IOException {
            while(delegate.hasNext() && next == null) {
                T obj = delegate.next();
                if (intersects(obj)) {
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

        boolean intersects(T obj) {
            return envelope(obj).intersects(bbox);
        }
    }

    public static <T> Cursor<T> filter(Cursor<T> cursor, Filter filter) {
        return new FilterCursor(cursor, filter);
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
}

