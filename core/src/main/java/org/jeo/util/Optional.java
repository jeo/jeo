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
package org.jeo.util;

/**
 * Utility object for dealing with references that may be null.
 * <p>
 * This class is inspired by guava's Optional class.
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class Optional<T> {

    public static <X> Optional<X> of(X value) {
        return new Optional<X>(value);
    }

    public static <X> Optional<X> nil() {
        return new Optional<X>(null);
    }

    T value;

    public Optional(T value) {
        this.value = value;
    }

    public boolean has() {
        return value != null;
    }

    public T get() {
        return get("null value");
    }

    public T get(String errmsg) {
        if (value == null) {
            throw new IllegalArgumentException(errmsg);
        }
        return value;
    }

    public T or(T fallback) {
        return value != null ? value : fallback;
    }
}
