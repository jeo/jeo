package org.jeo.geopkg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jsqlite.Stmt;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
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
                    //read the geometry, if this table has a geometry it will be the first column
                    Geometry geom = null;
    
                    int i = 0;
                    if (schema.getGeometry() != null) {
                        byte[] wkb = stmt.column_bytes(i++);
                        geom = wkbReader.read(wkb);
                    }
    
                    //read the other values
                    List<Object> values = new ArrayList<Object>(stmt.column_count()-1);
                    for (; i < stmt.column_count(); i++) {
                        values.add(stmt.column(i));
                    }
    
                    return new Feature(schema, geom, values);
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
