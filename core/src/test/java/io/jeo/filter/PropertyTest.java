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
package io.jeo.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PropertyTest {

    @Test
    public void testResolveObject() {
        Bar bar = new Bar("one");
        Foo foo = new Foo(bar, "two");

        assertEquals("two", new Property("baz").evaluate(foo));
        assertEquals(bar, new Property("bar").evaluate(foo));
        assertEquals("one", new Property("bar.bam").evaluate(foo));
    }

    @Test
    public void testResolveObjectWithNull() {
        assertNull(new Property("bar").evaluate(null));
        assertNull(new Property("bar.bam").evaluate(new Foo(null, "blah")));
    }

    public static class Bar {
        String bam;
        
        Bar(String bam) {
            this.bam = bam;
        }

        public String bam() {
            return bam;
        }
    }

    public static class Foo {
        Bar bar;
        String baz;
        
        Foo(Bar bar, String baz) {
            this.bar = bar;
            this.baz = baz;
        }

        public Bar getBar() {
            return bar;
        }

        public String getBaz() {
            return baz;
        }
    }
}
