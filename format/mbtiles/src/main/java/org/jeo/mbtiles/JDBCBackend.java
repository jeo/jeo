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
package org.jeo.mbtiles;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.jeo.util.Pair;
import org.jeo.sql.Backend;
import org.sqlite.SQLiteDataSource;

/**
 * A JDBCBackend
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
class JDBCBackend extends Backend {

    final DataSource db;

    JDBCBackend(MBTilesOpts opts) {
        db = createDataSource(opts);
    }

    DataSource createDataSource(MBTilesOpts opts) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + opts.file().getPath());
        return dataSource;
    }

    @Override
    public JDBCSession session() throws IOException {
        return new JDBCSession();
    }

    @Override
    public List<Pair<String, Class>> getColumnInfo(String table) throws IOException {
        String sql = String.format(Locale.ROOT,"SELECT * FROM %s LIMIT 1", table);
        JDBCResults results = (JDBCResults) query(sql);
        try {
            List<Pair<String, Class>> info = new ArrayList<Pair<String, Class>>();
            try {
                ResultSetMetaData rsmd = results.results.getMetaData();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    info.add(Pair.of(
                            rsmd.getColumnName(i + 1),
                            (Class) dbTypes.fromSQL(rsmd.getColumnType(i + 1))
                    ));
                }
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
            return info;
        } finally {
            results.close();
        }
    }

    @Override
    protected void closeInternal(Object object) throws Exception {
        if (object == null) {
            return;
        }
        if (object instanceof Connection) {
            ((Connection) object).close();
        } else if (object instanceof Statement) {
            ((Statement) object).close();
        } else if (object instanceof ResultSet) {
            ((ResultSet) object).close();
        } else {
            throw new RuntimeException("unhandled object");
        }
    }

    public void close() throws IOException {
        // nothing to do
    }

    class JDBCSession extends Backend.Session {

        final Connection connection;
        Statement statement;

        JDBCSession() throws IOException {
            try {
                connection = open(db.getConnection());
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public void addBatch(String sql) throws IOException {
            log(sql);
            try {
                if (statement == null) {
                    statement = open(connection.createStatement());
                }
                statement.addBatch(sql);
            } catch (SQLException ex) {
                closeSafe(statement);
                throw new IOException(ex);
            }
        }

        @Override
        public void execute(String sql) throws IOException {
            log(sql);
            try {
                Statement statement = connection.createStatement();
                statement.execute(sql);
            } catch (SQLException ex) {
                throw new IOException(ex);
            } finally {
                closeSafe(statement);
            }
        }

        PreparedStatement prepare(String sql, Object[] args) throws IOException {
            log(sql, args);
            try {
                PreparedStatement ps = open(connection.prepareStatement(sql));
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    ps.setObject(i + 1, arg);
                }
                return ps;
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public void executePrepared(String sql, Object[] args) throws IOException {
            try {
                prepare(sql, args).execute();
            } catch (SQLException ex) {
                throw new IOException(ex);
            } finally {
                closeSafe(statement);
            }
        }

        @Override
        public Results queryPrepared(String sql, Object... args) throws IOException {
            try {
                return new JDBCResults(prepare(sql, args).executeQuery());
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public Results query(String sql) throws IOException {
            log(sql);
            try {
                statement = open(connection.createStatement());
                return new JDBCResults(statement.executeQuery(sql));
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public void executeBatch() throws IOException {
            try {
                statement.executeBatch();
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }


        @Override
        public void endTransaction(boolean complete) throws IOException {
            try {
                if (complete) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public void beginTransaction() throws IOException {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public List<String> getPrimaryKeys(String tableName) throws IOException {
            List<String> keys = new ArrayList<String>(3);
            ResultSet pk = null;
            try {
                pk = connection.getMetaData().getPrimaryKeys(null, "", tableName);
                while (pk.next()) {
                    String name = pk.getString("COLUMN_NAME");
                    if (name == null) {
                        continue;
                    }
                    keys.add(name);
                }
            } catch (SQLException sqle) {
                closeSafe(pk);
            }
            return keys;
        }

    }

    // note column indices are zero-based
    class JDBCResults extends Backend.Results {
        final ResultSet results;

        JDBCResults(ResultSet results) {
            this.results = results;
        }

        @Override
        public Object getObject(int idx, Class clazz) throws IOException {
            try {
                return results.getObject(idx + 1);
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        protected Object getObject(int idx) throws IOException {
            return getObject(idx, null);
        }


        @Override
        public String getString(String col) throws IOException {
            try {
                return results.getString(col);
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public String getString(int idx) throws IOException {
            return getObject(idx).toString();
        }

        @Override
        public int getInt(int idx) throws IOException {
            return (Integer) getObject(idx);
        }

        @Override
        public double getDouble(int idx) throws IOException {
            return (Double) getObject(idx);
        }

        @Override
        public long getLong(int idx) throws IOException {
            try {
                return results.getLong(idx + 1);
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public boolean getBoolean(int idx) throws IOException {
            try {
                return results.getBoolean(idx + 1);
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public byte[] getBytes(int idx) throws IOException {
            try {
                return results.getBytes(idx + 1);
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public boolean next() throws IOException {
            try {
                return results.next();
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        protected void closeInternal() throws Exception {
            closeSafe(results.getStatement());
            closeSafe(results);
        }
    }
}