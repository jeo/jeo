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
package org.jeo.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * Filter utility class.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class Filters {
    /**
     * Returns true if the filter is <tt>null</tt> or equal to {@link #TRUE}.
     */
    public static boolean isTrueOrNull(Filter<?> f) {
        return f instanceof All || f == null;
    }

    /**
     * Returns true if the filter is <tt>null</tt> or equal to {@link #FALSE}.
     */
    public static boolean isFalseOrNull(Filter<?> f) {
        return f instanceof None || f == null;
    }

    /**
     * Returns a filter that always evaluates to true.
     */
    public static <T> Filter<T> all() {
        return new All<T>();
    }

    /**
     * Returns a filter that always evaluates to true.
     */
    public static <T> Filter<T> none() {
        return new None<T>();
    }

    public static Set<String> properties(Filter<?> f) {
        return (Set<String>) f.accept(new FilterVisitor() {

            @Override
            public Object visit(Property property, Object obj) {
                ((HashSet) obj).add(property.getProperty());
                return obj;
            }

        }, new HashSet());
    }
}
