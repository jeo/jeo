package org.jeo.geojson.simple.encoder;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;

import org.jeo.geojson.simple.JSONValue;

public class JSONEncoder {

    /**
     * output
     */
    final Writer out;

    /**
     * object stack
     */
    final ArrayDeque<Thing> stack = new ArrayDeque<Thing>();

    String indent;
    String space;
    String newline;

    /**
     * Creates a new encoder.
     * 
     * @param out Writer to output to.
     */
    public JSONEncoder(Writer out) {
        this(out, 0);
    }

    /**
     * Creates a new encoder with formatting.
     * 
     * @param out Writer to output to.
     * @param indentSize The number of spaces to use when indenting.
     */
    public JSONEncoder(Writer out, int indentSize) {
        this.out = out;

        indent = spaces(indentSize); 
        space = indentSize > 0 ? " " : "";
        newline = indentSize > 0 ? "\n" : "";
    }

    /**
     * The underlying writer.
     */
    public Writer getWriter() {
        return out;
    }

    /*
     * Helper to generate an indentation chunk.
     */
    static String spaces(int indentSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentSize; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Starts a new JSON object.
     * 
     * @return This encoder.
     */
    public JSONEncoder object() throws IOException {
        Thing t = peek();
        if (t != null) {
            if (t instanceof Arr) {
                Arr a = (Arr) t;
                if (a.size > 0) {
                    out.write(',');
                }
            }
            else {
                Obj o = (Obj) t;
                if (!o.key) {
                    throw new IllegalArgumentException("no key for object");
                }
            }
            t.size++;
        }

        stack.push(new Obj());

        out.write('{');

        return this;
    }

    /**
     * Starts a new JSON array.
     * 
     * @return This encoder.
     */
    public JSONEncoder array() throws IOException {
        Thing t = peek();
        if (t != null) {
            if (t instanceof Arr) {
                Arr a = (Arr) t;
                if (a.size > 0) {
                    out.write(',');
                }
            }
            else if (t instanceof Obj) {
                Obj o = (Obj) t;
                if (!o.key) {
                    throw new IllegalStateException("no key");
                }
            }
            t.size++;
        }

        stack.push(new Arr());

        out.write('[');

        return this;
    }

    /**
     * Starts an object property.
     *
     * @param key The key/name of the property.
     * 
     * @return This encoder.
     */
    public JSONEncoder key(String key) throws IOException {
        Thing t = peek();
        
        if (!(t instanceof Obj)) {
            throw new IllegalStateException("no object");
        }

        Obj o = (Obj) t;
        if (o.key) {
            throw new IllegalStateException("value required");
        }

        o.key = true;
        if (o.size > 0) {
            out.write(',');
        }

        newline();
        out.write("\"");
        out.write(JSONValue.escape(key));
        out.write("\":");
        out.write(space);
        return this;
    }

    /**
     * Specifies a numeric value for an object property.
     * <p>
     * You must call the {@link #key(String)} method before calling this method.
     * </p>
     * @param value The value.
     * @return This encoder.
     */
    public JSONEncoder value(Number value) throws IOException {

        if (value != null) {
            // check for double nan/infinte
            if (value instanceof Double || value instanceof Float) {
                Double val = value.doubleValue();
                if (val.isInfinite() || val.isNaN()) {
                    value = null;
                }
            }
        }

        return doValue(value != null ? value.toString() : null);

    }

    /**
     * Specifies a numeric value for an object property.
     * 
     * @param value The value.
     * 
     * @return This encoder.
     */
    public JSONEncoder value(Object value) throws IOException {
        if (value == null) {
            return nul();
        }

        if (value instanceof Number) {
            return value((Number)value);
        }
        else {
            return value(value.toString());
        }
    }

    /**
     * Specified a null value for an object property.
     * @return This encoder.
     */
    public JSONEncoder nul() throws IOException {
        return value((String)null);
    }

    /**
     * Specifies a string value for an object property.
     * <p>
     * You must call the {@link #key(String)} method before calling this method.
     * </p>
     * @return This encoder.
     */
    public JSONEncoder value(String value) throws IOException {
        return doValue(value != null ? "\""+JSONValue.escape(value)+"\"" : null);
    }

    /*
     * Helper to write out an already encoded value.
     */
    JSONEncoder doValue(String encoded) throws IOException {
        Thing t = peek();
        if (t == null) {
            throw new IllegalStateException("no object");
        }

        if (t instanceof Arr) {
            Arr a = (Arr) t;
            if (a.size > 0) {
                out.write(',');
            }

            newline();
            a.size++;
        }
        else {
            Obj o = (Obj) t;
            if (!o.key) {
                throw new IllegalStateException("no key");
            }
            o.key = false;
            o.size++;
        }

        if (encoded == null) {
            encoded = "null";
        }

        out.write(encoded);
        return this;
    }

    /**
     * Ends the current JSON object.
     *  
     * @return This encoder.
     */
    public JSONEncoder endObject() throws IOException {
        Thing t = peek();
        if (!(t instanceof Obj)) {
            throw new IllegalStateException("no object");
        }

        Obj o = (Obj) t;
        if (o.key) {
            throw new IllegalStateException("open key");
        }

        stack.pop();

        if (o.size > 0) {
            newline();
        }

        t = peek();
        if (t instanceof Obj) {
            o = (Obj) t;
            if (!o.key) {
                throw new IllegalStateException("no key");
            }
            o.key = false;
        }

        out.write('}');
        return this;
    }

    /**
     * Ends the current JSON array.
     *  
     * @return This encoder.
     */
    public JSONEncoder endArray() throws IOException {
        Thing t = peek();
        if (!(t instanceof Arr)) {
            throw new IllegalStateException("no array");
        }

        Arr a = (Arr) t;

        stack.pop();

        if (a.size > 0) {
            newline();
        }

        t = peek();
        if (t instanceof Obj) {
            Obj o = (Obj) t;
            if (!o.key) {
                throw new IllegalStateException("no key");
            }
            o.key = false;
        }

        out.write("]");
        return this;
    }

    public JSONEncoder flush() throws IOException {
        out.flush();
        return this;
    }

    /*
     * Moves output to the next line and indents. A no-op if formatting not active. 
     */
    void newline() throws IOException {
        out.write(newline);
        if (!"".equals(indent)) {
            for (int i = 0; i < stack.size(); i++) {
                out.write(indent);
            }
        }
    }

    /*
     * Does a "safe" peek of the stack, returning null if empty.
     */
    Thing peek() {
        return stack.isEmpty() ? null : stack.peek();
    }

    static abstract class Thing {
        protected int size = 0;
    }

    static class Obj extends Thing {
        protected boolean key = false;
    }

    static class Arr extends Thing {
    }
}
