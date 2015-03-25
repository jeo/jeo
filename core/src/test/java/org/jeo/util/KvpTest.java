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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class KvpTest {

    @Test
    public void testParse() {
        assertPairs(Kvp.get().parse("foo=bar&x=y"));
    }

    @Test
    public void testParseCustomDelim() {
        assertPairs(Kvp.get(";", ":").parse("foo:bar;x:y"));
    }

    @Test
    public void testParseTrailingLeading() {
        assertPairs(Kvp.get().parse("&foo=bar&x=y&"));

    }

    void assertPairs(List<Pair<String,String>> pairs) {
        assertEquals(2, pairs.size());

        assertEquals("foo", pairs.get(0).first);
        assertEquals("bar", pairs.get(0).second);

        assertEquals("x", pairs.get(1).first);
        assertEquals("y", pairs.get(1).second);
    }
}
