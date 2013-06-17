package org.jeo.sql;

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
     * Appends a string to the buffer.
     */
    public SQL add(String s) {
        buf.append(s);
        return this;
    }

    /**
     * Appends a formatted string to the buffer with arguments.
     * <p>
     * <tt>s</tt> should be the same format as specified to {@link String#format(String, Object...)}.
     * </p>
     */
    public SQL add(String s, Object... args) {
        buf.append(String.format(s, args));
        return this;
    }

    /**
     * Appends an integer to the buffer.
     */
    public SQL add(int i) {
        buf.append(i);
        return this;
    }

    /**
     * Appends a double to the buffer.
     */
    public SQL add(double d) {
        buf.append(d);
        return this;
    }

    /**
     * Appends a column/table/etc... name to the buffer. 
     * <p>
     * The name is escaped with double quotes.
     * </p>
     */
    public SQL name(String name) {
        buf.append("\"").append(name).append("\"");
        return this;
    }

    /**
     * Trims the last n characters from the buffer.
     */
    public SQL trim(int n) {
        buf.setLength(buf.length()-n);
        return this;
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
