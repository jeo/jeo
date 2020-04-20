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

/**
 * Visitor for {@link Selector} objects. 
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public interface SelectorVisitor {

    /**
     * Tests some criteria against the specified <tt>selector</tt>
     * 
     * @param selector The selector to test.
     * @param rule The parent rule of the selector. 
     * 
     * @return <code>true</code> if the selector passes the criteria.
     */
    boolean visit(Selector selector, Rule rule);
}
