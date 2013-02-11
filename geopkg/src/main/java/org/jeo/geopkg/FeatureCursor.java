package org.jeo.geopkg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jsqlite.Stmt;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.ListFeature;
import org.jeo.feature.Schema;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;

public class FeatureCursor implements Cursor<Feature> {

    Stmt stmt;
    Schema schema;

    Boolean next;
    WKBReader wkbReader;

    FeatureCursor(Stmt stmt, Schema schema) {
        this.stmt = stmt;
        this.schema = schema;
        this.wkbReader = new WKBReader();
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            try {
                next = stmt.step();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        return next;
    }

    @Override
    public Feature next() throws IOException {
        try {
            if (next != null && next) {
                try {
                    List<Field> fields = schema.getFields();
                    List<Object> values = new ArrayList<Object>(stmt.column_count());

                    for (int i = 0; i < fields.size(); i++) {
                        if (Geometry.class.isAssignableFrom(fields.get(i).getType())) {
                            values.add(wkbReader.read(stmt.column_bytes(i)));
                        }
                        else {
                            values.add(stmt.column(i));
                        }
                    }
    
                    return new ListFeature(values, schema);
                }
                finally {
                    next = null;
                }
            }
            return null;
        }
        catch(Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            stmt.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
