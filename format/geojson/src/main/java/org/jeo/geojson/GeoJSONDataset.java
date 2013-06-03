package org.jeo.geojson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.jeo.data.Cursor;
import org.jeo.data.Cursors;
import org.jeo.data.Driver;
import org.jeo.data.Query;
import org.jeo.data.VectorData;
import org.jeo.data.Cursor.Mode;
import org.jeo.feature.Feature;
import org.jeo.feature.Schema;
import org.jeo.util.Optional;
import org.jeo.util.Util;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class GeoJSONDataset implements VectorData {

    File file;

    public GeoJSONDataset(File file) {
        this.file = file;
    }

    @Override
    public Driver<?> getDriver() {
        return new GeoJSON();
    }

    @Override
    public String getName() {
        return Util.base(file.getName());
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public Schema getSchema() throws IOException {
        Optional<Feature> f = first();
        return f.has() ? f.get().schema() : null;
    }

    @Override
    public CoordinateReferenceSystem getCRS() throws IOException {
        //scan for a crs property
        JSONParser p = new JSONParser();
        Reader r = reader();
        try {
            CRSFinder f = new CRSFinder();
            try {
                p.parse(r, new RootHandler(f), true);
                return f.getCRS();
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
        finally {
            r.close();
        }
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
            if (file.length() != 0) {
                throw new IOException("Can't append to non empty dataset");
            }
            return new GeoJSONAppendCursor(writer());
        }
        return q.apply(new GeoJSONCursor(reader()));
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
