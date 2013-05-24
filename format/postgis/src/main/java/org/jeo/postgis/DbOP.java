package org.jeo.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.sql.DataSource;

public abstract class DbOP<T> {

    Deque<Object> toClose = new ArrayDeque<Object>();

    public final T run(DataSource db) throws IOException {
        try {
            return run(open(db.getConnection()));
        } catch (SQLException e) {
            throw propogate(e);
        }
    }
    
    public final T run(Connection  cx) throws IOException {
        try {
            return doRun(cx);
        }
        catch(Exception e) {
            throw propogate(e);
        }
        finally {
            while(!toClose.isEmpty()) {
                try {
                    Object obj = toClose.pop();
                    if (obj instanceof ResultSet) {
                        ((ResultSet) obj).close();
                    }
                    else if (obj instanceof Statement) {
                        ((Statement) obj).close();
                    }
                    else if (obj instanceof Connection) {
                        ((Connection) obj).close();
                    }
                }
                catch(SQLException e) {
                }
            }
        }
    }

    IOException propogate(Exception err) throws IOException {
        return err instanceof IOException ? (IOException) err : new IOException(err);
    }

    protected abstract T doRun(Connection cx) throws Exception;

    protected <T> T open(T obj) {
        toClose.add(obj);
        return obj;
    }
}
