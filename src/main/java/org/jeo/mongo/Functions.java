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
package org.jeo.mongo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jeo.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Server side javascript functions.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Functions {

    static Logger LOG = LoggerFactory.getLogger(MongoDB.class);

    static String bboxMap(String geometry) {
        return load("bbox_map.js").replaceAll("%geometry%", geometry);
    }

    static String bboxReduce() {
        return load("bbox_reduce.js");
    }

    static String load(String filename) {
        BufferedReader r = 
            new BufferedReader(new InputStreamReader(Functions.class.getResourceAsStream(filename), Util.UTF_8));
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
