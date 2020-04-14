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
package io.jeo.map;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RuleListTest {

    @Test
    public void testSelect() {
        Style style = new StyleBuilder()
            .rule().select("Map").set("background-color", "white").endRule()
            .rule().select("#widgets").filter("cost > 10").set("line-color", "#123").endRule()
            .style();

        RuleList rules = style.getRules();
        assertEquals(2, rules.size());

        assertEquals(1, rules.selectByName("Map", false, true).size());
        assertEquals(1, rules.selectByName("Map", false, false).size());
        assertEquals(0, rules.selectByName("map", false, true).size());
        assertEquals(1, rules.selectByName("map", false, false).size());
        assertEquals(1, rules.selectById("widgets", false).size());
    }
}
