/* Copyright 2013 The jeo project. All rights reserved.
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
package org.jeo.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.sql.DataSource;

/**
 * A database operation.
 * <p>
 * Subclasses must implement the {@link #doRun(Connection)} method. Subclasses are encouraged to 
 * use the {@link #open(Object)} when obtaining resources such as statements and result sets. Upon
 * completion all resources passed to open will be closed in reverse order.  
 * </p>
 * <p>
 * Example usage:
 * <code><pre>
 * DbOp&lt;Long> count = new DbOp&lt;Long>() {
 *   protected Long doRun(Connection cx) {
 *     ResultSet rs = open(open(cx.createStatement()).executeQuery("SELECT count(*) FROM foo"));
 *     rs.next();
 *     return rs.getLong(1);
 *   }
 * };
 * </code></pre>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 * @param <T> The type of object returned by the operation.
 */
public abstract class DbOP<T> {

    /**
     * resources to close
     */
    Deque<Object> toClose = new ArrayDeque<Object>();

    /**
     * Runs the db operation obtaining a new connection from the specified data source.
     * <p>
     * The obtained connection will be closed upon completion.
     * </p>
     * @param db The data source to obtain a new connection from.
     * 
     * @return The result of the operation.
     * 
     * @throws IOException Any errors, including SQLExceptions, that occur.
     */
    public final T run(DataSource db) throws IOException {
        try {
            return run(open(db.getConnection()));
        } catch (SQLException e) {
            throw propogate(e);
        }
    }

    /**
     * Runs the db operation with the specified connection.
     * <p>
     * It is the responsibility of the caller to close the specified connection.
     * </p>
     * 
     * @param cx The db connection.
     * 
     * @return The result of the operation.
     * 
     * @throws IOException Any errors, including SQLExceptions, that occur.
     */
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

    /**
     * Performs the operation.
     * 
     * @param cx The database connection.
     *  
     * @return The result of the operation.
     * 
     */
    protected abstract T doRun(Connection cx) throws Exception;

    /**
     * Specifies a resource to close upon completion of the operation.
     * 
     * @param obj The object to close.
     * 
     * @return The original object, <tt>obj</tt>
     */
    protected <T> T open(T obj) {
        toClose.add(obj);
        return obj;
    }
}
