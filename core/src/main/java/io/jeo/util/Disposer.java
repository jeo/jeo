/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.util;

import io.jeo.data.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks resources that must be closed later.
 * <p>
 *  Objects are tracked by calling {@link #open(Object)}. Objects are thrown onto
 *  a stack and later closed when this objects {@link #close()} method is called.
 *  Objects are closed in the reverse order they are opened.
 * </p>
 * <p>
 *  This class handles the following types of objects.
 *  <ul>
 *    <li>{@link java.lang.AutoCloseable}</li>
 *    <li>{@link java.io.Closeable}</li>
 *    <li>{@link Disposable}</li>
 *  </ul>
 *  The {@link #closeObject(Object)} can be overridden to handle additional types.
 * </p>
 * <p>
 *  Exceptions thrown by an objects close() method are always caught.
 * </p>
 */
public class Disposer implements Disposable {

    static final Logger LOG = LoggerFactory.getLogger(Disposer.class);

    /**
     * objects to close
     */
    Deque<Object> toClose = new ArrayDeque<Object>();

    /**
     * Tracks a new object to be closed.
     */
    public <T> T open(T obj) {
        if (obj != null) {
            toClose.push(obj);
        }
        return obj;
    }

    @Override
    public void close() {
        while(!toClose.isEmpty()) {
            Object obj = toClose.pop();
            try {
                closeObject(obj);
            }
            catch(Exception e) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Error closing object: " + obj, e);
                }
            }
        }
    }

    /**
     * Closes the object.
     * @param obj
     * @throws Exception
     */
    protected void closeObject(Object obj) throws Exception {
        if (obj instanceof Closeable) {
            ((Closeable) obj).close();
        }
        else if (obj instanceof AutoCloseable) {
            ((AutoCloseable) obj).close();
        }
        else if (obj instanceof Disposable) {
            ((Disposable) obj).close();
        }
    }

}
