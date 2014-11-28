/* Copyright 2014 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.geopkg;

import com.vividsolutions.jts.geom.Envelope;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.jeo.geom.Geom;
import org.jeo.sql.DbTypes;
import org.jeo.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GeoPackage DB abstraction layer.
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public abstract class Backend implements Closeable {

    protected static Logger LOG = LoggerFactory.getLogger(GeoPackage.class);

    protected static String log(String sql, Object... params) {
        if (LOG.isDebugEnabled()) {
            if (params.length == 1 && params[0] instanceof Collection) {
                params = ((Collection) params[0]).toArray();
            }

            StringBuilder log = new StringBuilder(sql);
            if (params.length > 0) {
                log.append("; ");
                for (Object p : params) {
                    log.append(p).append(", ");
                }
                log.setLength(log.length() - 2);
            }
            LOG.debug(log.toString());
        }
        return sql;
    }

    protected final DbTypes dbTypes = new GeoPkgTypes();

    /**
     * Return true if the backend can run scripts loaded via
     * Class.getResourceAsStream
     *
     * @return
     */
    protected boolean canRunScripts() {
        return true;
    }

    /**
     * The implementation should support closing any non-Closeable object passed
     * in and does not need to check for nulls. Use {@see #closeSafe} instead
     * of directly calling this method.
     * @param object the non-null object to close
     * @throws Exception if an error occurs closing
     */
    protected abstract void closeInternal(Object object) throws Exception;

    /**
     * Close an object created by this Backend. Accepts null.
     * @param o object or null
     */
    protected final void closeSafe(Object o) {
        if (o != null) {
            try {
                if (o instanceof Closeable) {
                    ((Closeable) o).close();
                } else {
                    closeInternal(o);
                }
            } catch (Exception ex) {
                LOG.warn("Error closing resource " + ex.getMessage());
            }
        }
    }

    /**
     * Get name and type information for the specified table.
     * @param table the name of the table
     * @return non-null list of column information
     * @throws IOException if any errors occur
     */
    protected abstract List<Pair<String, Class>> getColumnInfo(String table) throws IOException;

    /**
     * Open a new Session.
     * @return non-null Session ready for use
     * @throws java.io.IOException if an error occurs
     */
    protected abstract Session session() throws IOException;

    final FeatureEntry createFeatureEntry(Results rs) throws IOException {
        FeatureEntry e = new FeatureEntry();

        initEntry(e, rs);
        e.setGeometryColumn(rs.getString(10));
        e.setGeometryType(Geom.Type.from(rs.getString(11)));
        e.setZ((rs.getBoolean(12)));
        e.setM((rs.getBoolean(13)));
        return e;
    }

    /**
     * Execute a SQL statement calling {@link String.format} with the provided query and
     * optional args.
     * @param sql SQL to execute
     * @param args optional arguments to use during formatting
     * @throws IOException
     */
    final void exec(String sql, Object... args) throws IOException {
        sql = String.format(sql, args);
        Session s = session();
        try {
            s.execute(sql);
        } finally {
            s.close();
        }
    }

    /**
     * Execute a SQL statement using the provided args.
     * @param sql the query to execute
     * @param args the arguments
     * @throws IOException
     */
    final void execPrepared(String sql, Object... args) throws IOException {
        Session s = session();
        try {
            s.executePrepared(sql, args);
        } finally {
            s.close();
        }
    }

    /**
     * Sets common attributes of an entry.
     */
    final void initEntry(Entry e, Results rs) throws IOException {
        e.setTableName(rs.getString(0));
        e.setIdentifier(rs.getString(2));
        e.setDescription(rs.getString(3));
        e.setLastChange(rs.getString(4));
        e.setBounds(new Envelope(
                rs.getDouble(5), rs.getDouble(7), rs.getDouble(6), rs.getDouble(8)));
        e.setSrid(rs.getInt(9));
    }

    void processScript(Session session, String file, StringBuilder buf) throws IOException {
        List<String> lines = readScript(file);
        for (String sql : lines) {
            sql = sql.trim();
            if (sql.isEmpty()) {
                continue;
            }
            if (sql.startsWith("--")) {
                continue;
            }
            buf.append(sql).append(" ");

            if (sql.endsWith(";")) {
                String stmt = buf.toString();
                boolean skipError = stmt.startsWith("?");
                if (skipError) {
                    stmt = stmt.replaceAll("^\\? *", "");
                }

                session.addBatch(stmt);
                LOG.debug(stmt);

                buf.setLength(0);
            }
        }
    }

    /**
     * Execute a query calling {@link String.format} with the provided query and
     * optional args.
     * @param query the query to execute
     * @param args the arguments
     * @throws IOException
     */
    Results query(String query, Object... args) throws IOException {
        String sql = String.format(query, args);
        Session s = session();
        // chain the session to the query so it's closed, too
        return s.query(sql).closeSession(s);
    }

    /**
     * Execute a prepared query using the provided args.
     * @param query the query to execute
     * @param args the arguments
     * @throws IOException
     */
    Results queryPrepared(String query, Object... args) throws IOException {
        Session s = session();
        // chain the session to the query so it's closed, too
        return s.queryPrepared(query, args).closeSession(s);
    }

    List<String> readScript(String file) throws IOException {
        InputStream in = getClass().getResourceAsStream(file);
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        try {
            List<String> lines = new ArrayList<String>();
            String line = null;
            while ((line = r.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } finally {
            r.close();
        }
    }

    void runScripts(String... files) throws IOException {
        Session session = session();
        try {
            StringBuilder buf = new StringBuilder();
            for (String file : files) {
                processScript(session, file, buf);
            }
            session.executeBatch();
        } finally {
            session.close();
        }
    }

    /**
     * Begin a Session using a transaction.
     * @return Session
     * @throws IOException
     */
    Session transaction() throws IOException {
        Session session = session();
        session.beginTransaction();
        return session;
    }

    /**
     * A Session is an API adapter to a DB backend query session.
     */
    public abstract class Session implements Closeable {

        private final LinkedList<Object> opened = new LinkedList<Object>();

        protected final <T> T open(T o) {
            opened.add(o);
            return o;
        }

        public final void close() {
            while (!opened.isEmpty()) {
                closeSafe(opened.pop());
            }
        }

        /**
         * Add batch SQL to execute. Must use {@see #executeBatch} to execute.
         * An implementation may choose to execute here.
         * @param sql
         * @throws IOException
         */
        protected abstract void addBatch(String sql) throws IOException;

        /**
         * Execute any batch SQL added via {@see #addBatch}. An implementation
         * may do nothing here if already executed.
         * @throws IOException
         */
        protected abstract void executeBatch() throws IOException;

        /**
         * Execute a query using placeholders.
         * @param sql the SQL with placeholders
         * @param args the arguments
         * @return non-null Results
         * @throws IOException
         */
        protected abstract Results queryPrepared(String sql, Object... args) throws IOException;

        /**
         * Execute a query.
         * @param sql the SQL to execute
         * @return non-null Results
         * @throws IOException
         */
        protected abstract Results query(String sql) throws IOException;

        /**
         * Execute a statement using placeholders.
         * @param sql the SQL with placeholders
         * @param args the arguments
         * @throws IOException
         */
        protected abstract void executePrepared(String sql, Object... args) throws IOException;
/**
         * Execute a query.
         * @param sql the SQL to execute
         * @throws IOException
         */
        protected abstract void execute(String sql) throws IOException;

        /**
         * End a transaction.
         * @param complete true if success, false to rollback
         * @throws IOException
         */
        protected abstract void endTransaction(boolean complete) throws IOException;

        /**
         * Start a transaction.
         * @throws IOException
         */
        protected abstract void beginTransaction() throws IOException;

        /**
         * Get the primary keys for the table.
         * @param tableName
         * @return non-null List of primary keys
         * @throws IOException
         */
        protected abstract List<String> getPrimaryKeys(String tableName) throws IOException;

    }

    /**
     * Wrapper for database API result set or cursor. Column access is 0-based
     * unlike JDBC.
     */
    public abstract class Results implements Closeable {

        private Session session;

        /**
         * Returns the current exception, throwing an exception if none set.
         */
        Session session() {
            if (session == null) {
                throw new IllegalStateException("No session set");
            }
            return session;
        }

        /**
         * Attach the parent Session to be closed along with this Results.
         * @param session the session to close
         * @return this Results
         */
        final Results closeSession(Session session) {
            this.session = session;
            return this;
        }

        @Override
        public final void close() {
            closeSafe(session);
            try {
                closeInternal();
            } catch (Exception ex) {
                LOG.warn("Error closing Results", ex);
            }
        }

        protected abstract Object getObject(int idx, Class clazz) throws IOException;

        protected abstract String getString(int idx) throws IOException;

        protected abstract String getString(String col) throws IOException;

        protected abstract int getInt(int idx) throws IOException;

        protected abstract double getDouble(int idx) throws IOException;

        protected abstract long getLong(int idx) throws IOException;

        protected abstract boolean getBoolean(int idx) throws IOException;

        protected abstract byte[] getBytes(int idx) throws IOException;

        /**
         * Advance the current results row.
         * @return true if another row exists
         * @throws IOException if an error occurs
         */
        protected abstract boolean next() throws IOException;

        /**
         * Close underlying resources
         * @throws Exception if an error occurs
         */
        protected abstract void closeInternal() throws Exception;
    }

}
