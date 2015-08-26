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
package io.jeo.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Produces an XML document.
 * <p>
 * Usage:
 * <pre><code>
 * XMLWriter w = new XMLWriter();
 * w.init(output).start("message").text("Hello World").end("message").close();
 * </code></pre>
 *
 * The above would produce the following XML document:
 *   <pre>   &lt;message>Hello World&lt;message></pre>.
 * </p>
 */
public final class XMLWriter implements Closeable {

    private final Properties outputProps;
    private final AttributesImpl atts;
    private TransformerHandler tx;

    /**
     * Create an uninitialized WMLWriter.
     *
     * @see #init
     */
    public XMLWriter() {
        outputProps = new Properties();
        outputProps.put(OutputKeys.METHOD, "XML");
        atts = new AttributesImpl();
    }

    /**
     * Create a XMLWriter initialized with the given Writer.
     * @param out non-null Writer
     */
    public XMLWriter(Writer out) {
        this();
        init(out);
    }

    private TransformerHandler createTransformer(Writer out) throws TransformerConfigurationException {
        //create the document seriaizer
        SAXTransformerFactory txFactory
                = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

        TransformerHandler tx = txFactory.newTransformerHandler();
        //tx.getTransformer().setOutputProperties(outputProps);
        //tx.getTransformer().setOutputProperty(OutputKeys.METHOD, "XML");
        tx.getTransformer().setOutputProperties(outputProps);
        tx.setResult(new StreamResult(out));

        return tx;
    }

    /**
     * Turns on indentation and sets the indent size.
     * <p>
     * This method needs to be called prior to {@link #init}.
     * </p>
     * @param size The number of spaces to indent.
     */
    public XMLWriter indent(int size) {
        if (size > 0) {
            String INDENT_AMOUNT_KEY = "{http://xml.apache.org/xslt}indent-amount";
            outputProps.put(OutputKeys.INDENT, "yes");
            outputProps.put(INDENT_AMOUNT_KEY, String.valueOf(size));
        }
        return this;
    }

    /**
     * One form of {@link #init} must be invoked before use.
     * @param writer non-null Writer to output to
     */
    public XMLWriter init(Writer writer) {
        if (tx != null) {
            throw new IllegalStateException("init");
        }
        try {
            tx = createTransformer(writer);
            tx.startDocument();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    /**
     * One form of {@link #init} must be invoked before use.
     * @param out non-null OutputStream to output to
     */
    public XMLWriter init(OutputStream out) {
        try {
            init(new OutputStreamWriter(out, "utf-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    /**
     * Starts an element with the provided name and attribute key-value pairs.
     * <p>
     * Whatever attributes that have been set via calling {@link #atts(Object...)}
     * will also be written out. The list of attributes will be cleared after this
     * method is called.
     * </p>
     *
     * @param name The name of the element to encode.
     * @param kv key-value pairs representing attributes, must be an even number.
     * @return this
     */
    public XMLWriter start(String name, Object... kv) {
        atts(kv);
        try {
            // prevent potential NPE by passing empty strings
            tx.startElement("", "", name, atts);
            atts.clear();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Encodes a full element (start and end) with the specified name.
     * <p>
     * This method is short hand for the following:
     * <pre><code>
     *     start(name, kv);
     *     text(value);
     *     end(name);
     * </code></pre>
     * </p>
     * @param name The name of the element.
     * @param value The text value of the element, <tt>null</tt> means no text content.
     * @param kv Attribute key-value pairs.
     */
    public XMLWriter element(String name, Object value, Object... kv) {
        start(name, kv);
        text(value);
        end(name);
        return this;
    }

    /**
     * Encodes an element with no text content or children.
     *
     * @param name The name of the element.
     * @param kv The attribute key value pairs.
     */
    public XMLWriter emptyElement(String name, Object... kv) {
        return element(name, null, kv);
    }

    /**
     * Encodes text content.
     * <p>
     * This method is called between {@link #start(String, Object...)} and
     * {@link #end()} invocations.
     * </p>
     * @param value The value to encode as text, <tt>null</tt> makes this method a
     *              no-op.
     *
     */
    public XMLWriter text(Object value) {
        if (value != null) {
            String text = value.toString();
            try {
                tx.characters(text.toCharArray(), 0, text.length());
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    /**
     * Ends the current element.
     * <p>
     * This method must be called after {@link #start(String, Object...)}.
     * </p>
     */
    public XMLWriter end(String name) {
        try {
            // prevent potential NPE by passing empty strings
            tx.endElement("", "", name);
            return this;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add any attributes to use on the next invocation of {@link #start(String, Object...)}.
     *
     * @param kv An even number of key value attribute pairs.
     */
    public XMLWriter atts(Object... kv) {
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("non even number of key value pairs");
        }
        for (int i = 0; i < kv.length; i += 2) {
            if (kv[i] != null) {
                atts.addAttribute(null, null, String.valueOf(kv[i]), null, String.valueOf(kv[i + 1]));
            }
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        try {
            tx.endDocument();
        } catch (Exception ioe) {
            throw new IOException(ioe);
        }
    }
}
