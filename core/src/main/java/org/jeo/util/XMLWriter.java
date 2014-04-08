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
package org.jeo.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 */
public final class XMLWriter implements Closeable {

    private final Properties outputProps;
    private final AttributesImpl atts;
    private TransformerHandler tx;

    private XMLWriter() {
        outputProps = new Properties();
        outputProps.put(OutputKeys.METHOD, "XML");
        atts = new AttributesImpl();
    }

    private TransformerHandler createTransformer(OutputStream out) throws TransformerConfigurationException {
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

    public static XMLWriter create() {
        return new XMLWriter();
    }

    /**
     * Must be invoked prior to use.
     */
    public void init(OutputStream out) {
        if (tx != null) {
            throw new IllegalStateException("init");
        }
        try {
            tx = createTransformer(out);
            tx.startDocument();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public XMLWriter indent(int size) {
        if (size > 0) {
            String INDENT_AMOUNT_KEY = "{http://xml.apache.org/xslt}indent-amount";
            outputProps.put(OutputKeys.INDENT, "yes");
            outputProps.put(INDENT_AMOUNT_KEY, String.valueOf(size));
        }
        return this;
    }

    /**
     * Start an element with the provided name and attribute key-value pairs.
     * Whatever attributes that have been set via calling atts will also be
     * written. Attributes will be cleared after writing this element.
     *
     * @param name element name
     * @param kv key-value pairs
     * @return this
     */
    public XMLWriter start(String name, Object... kv) {
        atts(kv);
        try {
            tx.startElement(null, null, name, atts);
            atts.clear();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void emptyElement(String name, Object... kv) {
        start(name, kv);
        end(name);
    }

    public void element(String name, Object value, Object... kv) {
        start(name, kv);
        text(value);
        end(name);
    }

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

    public XMLWriter end(String name) {
        try {
            tx.endElement(null, null, name);
            return this;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add any attributes to use on the next invocation of start.
     *
     * @param kv
     * @return
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
