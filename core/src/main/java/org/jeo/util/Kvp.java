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
package org.jeo.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing/encoding key-value pairs.
 * <p>
 * Example usage:
 * <code>
 *  <pre>
 *  List<Pair<String,String>> pairs = Kvp.get(",", ":").parse("foo:bar, x:y");
 *  </pre>
 * </code>
 * </p>
 */
public class Kvp {

    /**
     * Returns a parser with the default delimiters '&' and '='.
     */
    public static Kvp get() {
        return get("&", "=");
    }

    /**
     * Returns a parser with the specified delimiters.
     *
     * @param pairDelim The first level of delimiter, separating the pairs.
     * @param kvDelim The second level delimiter, separating the key/value of each pair.
     */
    public static Kvp get(String pairDelim, String kvDelim) {
        return new Kvp(pairDelim, kvDelim);
    }

    String pairDelim;
    String kvDelim;

    Kvp(String pairDelim, String kvDelim) {
        this.pairDelim = pairDelim;
        this.kvDelim = kvDelim;
    }

    /**
     * Parses the specified string into key value pairs.
     */
    public List<Pair<String,String>> parse(String val) {
        List<Pair<String,String>> list = new ArrayList<>();
        for (String pair : val.split("\\s*"+pairDelim+"\\s*")) {
            pair = pair.trim();
            if (pair.isEmpty()) {
                continue;
            }
            String[] kvp = pair.split("\\s*"+kvDelim+"\\s*");
            if (kvp.length != 2) {
                throw new IllegalArgumentException("Invalid key/value pair: " + pair);
            }

            list.add(Pair.of(kvp[0], kvp[1]));
        }
        return list;
    }
}
