package io.jeo.vector;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import io.jeo.data.Cursor;
import io.jeo.geom.Envelopes;
import io.jeo.geom.Geom;
import io.jeo.proj.Proj;
import io.jeo.util.Function;
import io.jeo.util.Predicate;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Extension of Cursor for {@link Feature} objects.
 *
 */
public abstract class FeatureCursor extends Cursor<Feature> {

    /**
     * Reprojects features in the stream to a specified coordinate reference system.
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

    private static class TransformCursor extends FeatureCursorWrapper {

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

    private static class TransformFeature extends GeometryTransformFeature {

        CoordinateTransform transform;

        TransformFeature(Feature delegate, CoordinateTransform transform) {
            super(delegate);
            this.transform = transform;
        }

        @Override
        protected Geometry wrap(Geometry g) {
            return Proj.transform(g, transform);
        }
    }

    private static class ReprojectCursor<T extends Feature> extends FeatureCursorWrapper {

        Map<String, CoordinateTransform> transforms;
        CoordinateReferenceSystem target;

        ReprojectCursor(Cursor<Feature> delegate, CoordinateReferenceSystem target) {
            super(delegate);

            this.target = Objects.requireNonNull(target, "target crs must not be null");
            transforms = new HashMap<>();
        }

        @Override
        public Feature next() throws IOException {
            return new ReprojectFeature(super.next(), target, transforms);
        }
    }

    private static class ReprojectFeature extends GeometryTransformFeature {

        CoordinateReferenceSystem target;
        Map<String,CoordinateTransform> transforms;

        public ReprojectFeature(Feature delegate, CoordinateReferenceSystem target, Map<String,CoordinateTransform> transforms) {
            super(delegate);
            this.target = target;
            this.transforms = transforms;
        }

        @Override
        protected Geometry wrap(Geometry g) {
            CoordinateReferenceSystem crs = Proj.crs(g);
            if (crs != null) {
                CoordinateTransform tx = transforms.get(crs.getName());
                if (tx == null) {
                    tx = Proj.transform(crs, target);
                    transforms.put(crs.getName(), tx);
                }

                g = Proj.transform(g, tx);
            }
            return g;
        }
    }

    /**
     * Sets the projection of features in the cursor, overriding any projection that exists.
     *
     * @param crs The override projection.
     *
     * @return The wrapped cursor.
     */
    public FeatureCursor crs(CoordinateReferenceSystem crs) {
        return new CrsOverrideCursor(this, crs);
    }

    private static class CrsOverrideCursor<T extends Feature> extends FeatureCursorWrapper {

        CoordinateReferenceSystem crs;

        CrsOverrideCursor(Cursor<Feature> delegate, CoordinateReferenceSystem crs) {
            super(delegate);
            this.crs = crs;
        }

        @Override
        public Feature next() throws IOException {
            Feature f = super.next();
            return new GeometryTransformFeature(f) {
                @Override
                protected Geometry wrap(Geometry g) {
                    return Proj.crs(g, crs);
                }
            };
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
    public FeatureCursor multify() {
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

    static class SelectFieldsCursor extends FeatureCursorWrapper {
        private final List<String> fields;

        public SelectFieldsCursor(Cursor<Feature> delegate, Iterable<String> fields) {
            super(delegate);
            this.fields = new ArrayList();
            for (String f : fields) {
                this.fields.add(f);
            }
        }

        @Override
        public Feature next() throws IOException {
            Feature next = super.next();
            if (next != null) {
                Map<String,Object> values = new LinkedHashMap<>(next.map());
                values.keySet().retainAll(fields);

                return new MapFeature(next.id(), values);
            }
            return next;
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
    public static <T extends Feature> FeatureCursor wrap(Cursor<Feature> cursor) {
        if (cursor instanceof FeatureCursor) {
            return (FeatureCursor) cursor;
        }
        return new FeatureCursorWrapper(cursor);
    }

    static class FeatureCursorWrapper extends FeatureCursor {

        Cursor<Feature> delegate;

        public FeatureCursorWrapper(Cursor<Feature> cursor) {
            this.delegate = cursor;
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
        public void rewind() {
            delegate.rewind();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

    }
}
