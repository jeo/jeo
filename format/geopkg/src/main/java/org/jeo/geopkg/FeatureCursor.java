package org.jeo.geopkg;

import static org.jeo.geopkg.GeoPkgWorkspace.LOG;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.Cursor;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Feature;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geopkg.geom.GeoPkgGeomReader;
import org.jeo.sql.PrimaryKey;
import org.jeo.sql.PrimaryKeyColumn;

import com.vividsolutions.jts.geom.Geometry;

public class FeatureCursor extends Cursor<Feature> {

    ResultSet results;
    Connection cx;

    Schema schema;
    PrimaryKey primaryKey;

    Boolean next;
    GeoPkgGeomReader geomReader;

    FeatureCursor(ResultSet stmt, Connection cx, FeatureEntry entry, GeoPkgWorkspace workspace) 
        throws IOException {
        this.results = stmt;
        this.cx = cx;

        this.schema = workspace.schema(entry);
        this.primaryKey = workspace.primaryKey(entry);

        this.geomReader = new GeoPkgGeomReader();
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            try {
                next = results.next();
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
                    List<Object> values = new ArrayList<Object>();

                    for (int i = 0; i < fields.size(); i++) {
                        if (Geometry.class.isAssignableFrom(fields.get(i).getType())) {
                            values.add(geomReader.read(results.getBytes(i+1)));
                        }
                        else {
                            values.add(results.getObject(i+1));
                        }
                    }
    
                    String fid = null;
                    if (primaryKey != null) {
                        StringBuilder buf = new StringBuilder();
                        for (PrimaryKeyColumn pkcol : primaryKey.getColumns()) {
                            Object obj = results.getObject(pkcol.getName());
                            if (obj != null) {
                                buf.append(obj);
                            }
                            buf.append(".");
                        }

                        buf.setLength(buf.length()-1);
                        fid = buf.toString();
                    }

                    //TODO: feature id
                    return new BasicFeature(fid, values, schema);
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
            if (results != null) {
                results.close();
            }
            results = null;
        } catch (Exception e) {
            LOG.debug("error closing result set", e);
        }

        try {
            if (cx != null) {
                cx.close();
            }
        }
        catch(Exception e) {
            LOG.debug("error closing Connection", e);
        }
    }
}
