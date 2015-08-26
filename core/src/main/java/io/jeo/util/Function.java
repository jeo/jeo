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
package io.jeo.util;

/**
 * Interface for mapping one value (domain) to another (range).
 * <p>
 * Inspired by Guava's / Java 8 function.
 * </p>
 * @param <D> Domain type.
 * @param <R> Range type.
 */
public interface Function<D,R> {

    /**
     * Executes the value on the given input.
     *
     * @param value The input value.
     *
     * @return The function value for the given input.
     */
    R apply(D value);
}
