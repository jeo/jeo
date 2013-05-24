package org.jeo.mongo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side javascript functions.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Functions {

    static Logger LOG = LoggerFactory.getLogger(MongoDB.class);

    static final String BBOX_MAP = load("bbox_map.js");
    static final String BBOX_REDUCE = load("bbox_reduce.js");

    static String load(String filename) {
        BufferedReader r = 
            new BufferedReader(new InputStreamReader(Functions.class.getResourceAsStream(filename)));
        try {
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();
        } catch (IOException e) {
            //should not happen
            throw new RuntimeException("Error loading " + filename, e);
        }
        finally {
            try {
                r.close();
            } catch (IOException e) {
                LOG.trace("Error closing " + filename, e);
            }
        }
    }
}
