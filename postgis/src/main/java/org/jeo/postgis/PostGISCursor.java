package org.jeo.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;
import org.jeo.feature.MapFeature;

import com.vividsolutions.jts.io.WKBReader;

public class PostGISCursor extends Cursor<Feature> {

    ResultSet rs;
    PostGISDataset dataset;
    Boolean next;

    PostGISCursor(ResultSet rs, PostGISDataset dataset) {
        this.rs = rs;
        this.dataset = dataset;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            try {
                next = rs.next();
            } catch (SQLException e) {
                handle(e);
            }
        }
        return next;
    }

    @Override
    public Feature next() throws IOException {
        if (next != null && next.booleanValue()) {
            try {
                Map<String,Object> map = new LinkedHashMap<String, Object>();
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 0; i < md.getColumnCount(); i++) {
                    Object obj = rs.getObject(i+1);
                    String col = md.getColumnName(i+1);

                    if (dataset.getSchema().field(col).isGeometry()) {
                        obj = new WKBReader().read(rs.getBytes(i+1));
                    }

                    map.put(col, obj);
                }
                return new MapFeature(null, map, dataset.getSchema());
            }
            catch(Exception e) {
                handle(e);
            }
            finally {
                next = null;
            }
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        if (rs != null) {
            Statement st = null;
            Connection cx = null;
            try {
                st = rs.getStatement();
                cx = st.getConnection();
            } catch (SQLException e) {}

            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {}
            }
            if (cx != null) {
                try {
                    cx.close();
                } catch (SQLException e) {}
            }
            try {
                rs.close();
            } catch (SQLException e) {}
        }
    }

    void handle(Exception e) throws IOException {
        close();
        throw new IOException(e);
    }
}
