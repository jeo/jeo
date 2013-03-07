package org.jeo.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class Memory implements Workspace {

    Map<String,Dataset> data = new LinkedHashMap<String, Dataset>();

    @Override
    public Iterator<String> layers() throws IOException {
        return data.keySet().iterator();
    }

    @Override
    public Dataset get(String layer) throws IOException {
        return data.get(layer);
    }

    @Override
    public Vector create(Schema schema) throws IOException, UnsupportedOperationException {
        MemoryVector v = new MemoryVector(schema);
        data.put(schema.getName(), v);
        return v;
    }

    @Override
    public void dispose() {
        data.clear();
    }

    static class MemoryVector implements Vector {

        Schema schema;
        List<Feature> features = new ArrayList<Feature>();

        public MemoryVector(Schema schema) {
            this.schema = schema;
        }

        @Override
        public String getName() {
            return schema.getName();
        }

        @Override
        public String getTitle() {
            return getName();
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public CoordinateReferenceSystem getCRS() {
            return schema.crs();
        }

        @Override
        public Envelope bounds() throws IOException {
            if (schema.geometry() == null) {
                return null;
            }

            Envelope e = new Envelope();
            e.setToNull();

            if (features.isEmpty()) {
                return e;
            }

            for (Feature f : features) {
                Geometry g = f.geometry();
                if (g != null) {
                    e.expandToInclude(g.getEnvelopeInternal());
                }
            }

            return e;
        }

        @Override
        public Schema getSchema() throws IOException {
            return schema;
        }

        @Override
        public long count(Envelope bbox) throws IOException {
            return features.size();
        }

        @Override
        public Cursor<Feature> read(Envelope bbox) throws IOException {
            if (bbox == null) {
                return Cursors.create(features);
            }

            List<Feature> match = new ArrayList<Feature>();
            for (Feature f : features) {
                
                Geometry g = f.geometry();
                if (g == null) {
                    continue;
                }

                if (g.getEnvelopeInternal().intersects(bbox)) {
                    match.add(f);
                }
            }
            return Cursors.create(match);
        }

        @Override
        public void add(Feature f) throws IOException {
            features.add(f);
        }
    }
}
