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
package org.jeo.map;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StyleBuilderTest {

    @Test
    public void testSimple() throws Exception {
        Style style = 
            new StyleBuilder().rule().select("Map").set("background-color", "green").style();

        assertEquals(1, style.getRules().size());

        Rule r = style.getRules().get(0);
        assertEquals(RGB.green, r.color(null, "background-color", null));

        assertEquals(1, r.getSelectors().size());

        Selector s = r.getSelectors().get(0);
        assertEquals("Map", s.getName());
    }
}
