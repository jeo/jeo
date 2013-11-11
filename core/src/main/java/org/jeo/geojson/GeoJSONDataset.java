package org.jeo.geojson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.data.Cursor.Mode;
import org.jeo.data.Cursors;
import org.jeo.data.Driver;
import org.jeo.data.FileData;
import org.jeo.data.Query;
import org.jeo.data.QueryPlan;
import org.jeo.data.VectorDataset;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geojson.parser.CRSFinder;
import org.jeo.geojson.parser.RootHandler;
import org.jeo.json.parser.JSONParser;
import org.jeo.json.parser.ParseException;
import org.jeo.proj.Proj;
import org.jeo.util.Key;
import org.jeo.util.Optional;
import org.jeo.util.Util;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class GeoJSONDataset implements VectorDataset, FileData {

    File file;

    public GeoJSONDataset(File file) {
        this.file = file;
    }

    @Override
    public Driver<?> getDriver() {
        return new GeoJSON();
    }

    @Override
    public Map<Key<?>, Object> getDriverOptions() {
        return (Map) Collections.singletonMap(GeoJSON.FILE, file);
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getName() {
        return Util.base(file.getName());
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Schema schema() throws IOException {
        Optional<Feature> f = first();
        if (f.has()) {
            Schema schema = f.get().schema();
            SchemaBuilder sb = Schema.build(getName());
            for (Field fld : schema) {
                if (fld.isGeometry() && fld.getCRS() == null) {
                    sb.field(fld.getName(), (Class<Geometry>) fld.getType(), crs());
                }
                else {
                    sb.field(fld);
                }
            }
            return sb.schema();
        }
        return null;
    }

    @Override
    public CoordinateReferenceSystem crs() throws IOException {
        CoordinateReferenceSystem crs = null;

        //first scan for a crs property
        JSONParser p = new JSONParser();
        Reader r = reader();
        try {
            CRSFinder f = new CRSFinder();
            try {
                p.parse(r, new RootHandler(f), true);
                crs = f.getCRS();
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
        finally {
            r.close();
        }

        // GeoJSON actually specified that the data should be 4326 so fall back on that
        return crs != null ? crs : Proj.EPSG_4326;
    }

    @Override
    public Envelope bounds() throws IOException {
        return Cursors.extent(cursor(new Query()));
    }

    @Override
    public long count(Query q) throws IOException {
        return Cursors.size(cursor(q));
    }

    @Override
    public Cursor<Feature> cursor(Query q) throws IOException {
        if (q.getMode() == Mode.UPDATE) {
            throw new IOException("Update cursor not supported");
        }
        if (q.getMode() == Mode.APPEND) {
            if (!Util.isEmpty(file)) {
                throw new IOException("Can't append to non empty dataset");
            }
            return new GeoJSONAppendCursor(writer());
        }

        return new QueryPlan(q).apply(new GeoJSONCursor(reader()));
    }

    @Override
    public void close() {
    }

    Optional<Feature> first() throws IOException {
        Cursor<Feature> c = cursor(new Query());
        try {
            if (c.hasNext()) {
                return Optional.of(c.next());
            }
            return Optional.nil(Feature.class);
        }
        finally {
            c.close();
        }
    }

    Reader reader() throws IOException {
        return new BufferedReader(new FileReader(file));
    }

    Writer writer() throws IOException {
        return new BufferedWriter(new FileWriter(file));
    }
}
