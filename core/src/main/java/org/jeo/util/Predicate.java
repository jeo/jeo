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

/**
 * Evaluates a condition against a single value.
 * <p>
 * Inspired by Guava / Java 8 predicate.
 * </p>
 * @param <T>
 */
public abstract class Predicate<T> {

    /**
     * Evalutes the predicate.
     */
    public abstract boolean test(T val);

    /**
     * And's this predicate to another.
     */
    public Predicate<T> and(final Predicate<T> other) {
        final Predicate<T> self = this;
        return new Predicate<T>() {
            @Override
            public boolean test(T val) {
                return self.test(val) && other.test(val);
            }
        };
    }
}
