package org.jeo.vector;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import org.jeo.data.Cursor;
import org.jeo.geom.Envelopes;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.jeo.util.Function;
import org.jeo.util.Predicate;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension of Cursor for Feature objects.
 */
public abstract class FeatureCursor extends Cursor<Feature> {

    /**
     * Returns an empty feature cursor.
     */
    public static FeatureCursor empty() {
        return new FeatureCursor() {
            @Override
            public boolean hasNext() throws IOException {
                return false;
            }

            @Override
            public Feature next() throws IOException {
                return null;
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    public FeatureCursor() {
    }

    public FeatureCursor(Mode mode) {
        super(mode);
    }

    @Override
    public FeatureCursor write() throws IOException {
        return (FeatureCursor) super.write();
    }

    @Override
    public FeatureCursor remove() throws IOException {
        return (FeatureCursor) super.remove();
    }

    /**
     * Reprojects features in the cursor to a specified coordinate reference system.
     * <p>
     * This method determines the source crs from objects in the underlying cursor. Use
     * {@link #reproject(org.osgeo.proj4j.CoordinateReferenceSystem, org.osgeo.proj4j.CoordinateReferenceSystem)} to
     * explicitly specify the source crs.
     * </p>
     * @param crs The crs to reproject to.
     *
     * @return The wrapped cursor.
     */
    public FeatureCursor reproject(CoordinateReferenceSystem crs) {
        return reproject(null, crs);
    }

    /**
     * Reprojects features in the cursor between two specified coordinate reference systems.
     *
     * @param from The source crs.
     * @param to The destination crs.
     *
     * @return The wrapped cursor.
     */
    public FeatureCursor reproject(CoordinateReferenceSystem from, CoordinateReferenceSystem to) {

        return from != null ?
            new TransformCursor(this, from, to) : new ReprojectCursor(this, to);
    }

    private static class ReprojectCursor extends CursorWrapper {

        Map<String, CoordinateTransform> transforms;
        CoordinateReferenceSystem target;

        ReprojectCursor(FeatureCursor delegate, CoordinateReferenceSystem target) {
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

    private static class TransformCursor extends CursorWrapper {

        CoordinateTransform tx;

        TransformCursor(FeatureCursor delegate, CoordinateReferenceSystem from, CoordinateReferenceSystem to) {
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
     * Returns a cursor with objects that intersect the specified bounding box.
     * <p>
     * The <tt>loose</tt> paremeter controls whether a full intersection
     * </p>
     * @param bbox The bounding box filter.
     * @param loose
     *
     * @return The wrapped cursor.
     */
    public FeatureCursor intersect(final Envelope bbox, boolean loose) {
        Predicate<Geometry> p = new Predicate<Geometry>() {
            @Override
            public boolean test(Geometry val) {
                return val.getEnvelopeInternal().intersects(bbox);
            }
        };
        if (!loose) {
            final PreparedGeometry poly = Geom.prepare(Envelopes.toPolygon(bbox));
            p = p.and(new Predicate<Geometry>() {
                @Override
                public boolean test(Geometry val) {
                    return poly.intersects(val);
                }
            });
        }

        final Predicate<Geometry> intersect = p;
        return wrap(filter(new Predicate<Feature>() {
            @Override
            public boolean test(Feature f) {
                Geometry g = f.geometry();
                if (g == null) {
                    return false;
                }

                return intersect.test(g);
            }
        }));
    }

    /**
     * Transforms non geometry collection objects from the specified cursor to the appropriate
     * geometry collection.
     */
    public FeatureCursor multify() throws IOException {
        return wrap(map(new Function<Feature, Feature>() {
            @Override
            public Feature apply(Feature value) {
                return Features.multify(value);
            }
        }));
    }

    /**
     * Transforms the cursor one containing features with the specified attributes.
     *
     * @param fields The fields to select.
     *
     * @return The selected cursor.
     */
    public FeatureCursor select(final Iterable<String> fields) {
       return new SelectFieldsCursor(this, fields);
    }

    static class SelectFieldsCursor extends CursorWrapper {
        private final Iterable<String> fields;
        private final Map<String, Object> values;
        private Schema schema;

        public SelectFieldsCursor(Cursor<Feature> delegate, Iterable<String> fields) {
            super(delegate);
            this.fields = fields;
            this.values = new HashMap<>();
        }

        @Override
        public Feature next() throws IOException {
            Feature next = super.next();
            if (next != null) {
                if (schema == null ) {
                    schema = schema(next);
                }

                // values is copied by BasicFeature, so we reuse it
                for (String f : fields) {
                    values.put(f, next.get(f));
                }

                // this may not be the most efficient approach compared
                // to wrapping the feature
                next = new BasicFeature(next.getId(), values, schema);
            }
            return next;
        }

        public Schema schema(Feature original) {
            // if schemaless, don't use any existing derived schema as it will
            // now look invalid due to removed features
            // otherwise, derive a new one
            Schema s = original.schema(false);
            if (s != null) {
                s = SchemaBuilder.select(original.schema(), fields);
            }
            return s;
        }
    }

    @Override
    public FeatureCursor filter(Predicate<Feature> filter) {
        return wrap(super.filter(filter));
    }

    @Override
    public FeatureCursor limit(Integer limit) {
        return wrap(super.limit(limit));
    }

    @Override
    public FeatureCursor skip(Integer offset) {
        return wrap(super.skip(offset));
    }

    @Override
    public FeatureCursor buffer(int n) {
        return wrap(super.buffer(n));
    }

    /**
     * Wraps a cursor of Feature as a FeatureCursor, if it is not an instance
     * already.
     */
    public static FeatureCursor wrap(Cursor<Feature> cursor) {
        if (cursor instanceof FeatureCursor) {
            return (FeatureCursor) cursor;
        }
        return new CursorWrapper(cursor);
    }

    static class CursorWrapper extends FeatureCursor {
        protected Cursor<Feature> delegate;

        CursorWrapper(Cursor<Feature> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() throws IOException {
            return delegate.hasNext();
        }

        @Override
        public Feature next() throws IOException {
            return delegate.next();
        }

        @Override
        public FeatureCursor write() throws IOException {
            delegate.write();
            return this;
        }

        @Override
        public FeatureCursor remove() throws IOException {
            delegate.remove();
            return this;
        }

        @Override
        public boolean rewind() throws IOException {
            return delegate.rewind();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
