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
     * Returns true if the filter is <tt>null</tt> or instance of {@link All}.
     */
    public static boolean isTrueOrNull(Filter<?> f) {
        return f instanceof All || f == null;
    }

    /**
     * Returns true if the filter is <tt>null</tt> or instance of {@link None}.
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

    static FilterVisitor propertyCollector = new FilterVisitor() {

        @Override
        public Object visit(Property property, Object obj) {
            ((Set) obj).add(property.property());
            return obj;
        }

    };

    /**
     * Add all property references in the provided Filter to the provided
     * Set.
     * @param f non-null Filter to scan
     * @param all non-null Set to add property names to
     * @return the provided set
     */
    public static Set<String> properties(Filter<?> f, Set<String> all) {
        return (Set<String>) f.accept(propertyCollector, all);
    }

    /**
     * Add all property references in the provided Expression to the provided
     * Set.
     * @param e non-null Expression to scan
     * @param all non-null Set to add property names to
     * @return the provided set
     */
    public static Set<String> properties(Expression e, Set<String> all) {
        return (Set<String>) e.accept(propertyCollector, all);
    }

    /**
     * Return a Set of all property references in the provided Filter.
     * @param f non-null Filter to scan
     * @return non-null Set of any property references in the Filter
     */
    public static Set<String> properties(Filter<?> f) {
        return properties(f, new HashSet<String>());
    }
}
