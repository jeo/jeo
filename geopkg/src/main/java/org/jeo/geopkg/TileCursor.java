package org.jeo.geopkg;

import java.io.IOException;

import jsqlite.Stmt;

import org.jeo.data.Cursor;
import org.jeo.data.Tile;

public class TileCursor extends Cursor<Tile> {

    Stmt stmt;
    Boolean next;

    public TileCursor(Stmt stmt) {
        this.stmt = stmt;
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
    public Tile next() throws IOException {
        if (next == null || !next) {
            return null;
        }
        Tile t = new Tile();

        try {
            t.setZoom(stmt.column_int(0));
            t.setColumn(stmt.column_int(1));
            t.setRow(stmt.column_int(2));
            t.setData(stmt.column_bytes(3)); 
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
            stmt.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
