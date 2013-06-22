package org.jeo.geopkg;

import static org.jeo.geopkg.GeoPkgWorkspace.LOG;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import org.jeo.data.Cursor;
import org.jeo.data.Tile;

public class TileCursor extends Cursor<Tile> {

    ResultSet results;
    Connection cx;
    Boolean next;

    public TileCursor(ResultSet results, Connection cx) {
        this.results = results;
        this.cx = cx;
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
    public Tile next() throws IOException {
        if (next == null || !next) {
            return null;
        }
        Tile t = new Tile();

        try {
            t.setZ(results.getInt(1));
            t.setX(results.getInt(2));
            t.setY(results.getInt(3));
            t.setData(results.getBytes(4)); 
        }
        catch(Exception e) {
            throw new IOException(e);
        }

        next = null;
        return t;
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
