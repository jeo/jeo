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

import java.util.Locale;

/**
 * Helper class for generating SQL strings.
 * <p>
 * Usage:
 * <pre><code>
 * String sql = new SQL("SELECT ").name("price").add(" FROM ").name("widgets").add(" WHERE ")
 *      .name("id").add(" = ").add(12).toString(); 
 * </code></pre>
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class SQL {

    StringBuilder buf;

    /**
     * Creates an empty sql buffer.
     */
    public SQL() {
        buf = new StringBuilder();
    }

    /**
     * Creates an sql buffer initialized with the specified string contents. 
     */
    public SQL(String sql) {
        buf = new StringBuilder(sql);
    }

    /**
     * Creates an sql buffer initialized with the specified formatted string + arguments.
     */
    public SQL(String sql, Object arg1, Object... args) {
        this();
        add(sql, arg1, args);
    }

    /**
     * Appends an object to the buffer.
     */
    public SQL add(Object o) {
        buf.append(o);
        return this;
    }

    /**
     * Appends a formatted string to the buffer with arguments.
     * <p>
     * <tt>s</tt> should be the same format as specified to {@link String#format(String, Object...)}.
     * </p>
     */
    public SQL add(String s, Object... args) {
        buf.append(String.format(Locale.ROOT, s, args));
        return this;
    }

    /**
     * Appends a qualified column/table/etc... name to the buffer.
     * <p>
     * The name and prefix are escaped with double quotes.
     * </p>
     * <p>
     * <tt>prefix</tt> may be null.
     * </p>
     */
    public SQL name(String prefix, String name) {
        if (prefix != null) {
            buf.append("\"").append(prefix).append("\".");
        }
        buf.append("\"").append(name).append("\"");
        return this;
    }

    /**
     * Appends a column/table/etc... name to the buffer. 
     * <p>
     * The name is escaped with double quotes.
     * </p>
     */
    public SQL name(String name) {
        return name(null, name);
    }

    public SQL str(String str) {
        str = str.replaceAll("'", "''");
        buf.append("'").append(str).append("'");
        return this;
    }

    /**
     * Trims the last n characters from the buffer.
     */
    public SQL trim(int n) {
        buf.setLength(buf.length()-n);
        return this;
    }

    /**
     * Clears the buffer.
     */
    public SQL clear() {
        buf.setLength(0);
        return this;
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
