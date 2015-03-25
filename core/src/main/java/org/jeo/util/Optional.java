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

import java.util.NoSuchElementException;

/**
 * Utility object for dealing with references that may be null.
 * <p>
 * This class is inspired by guava's Optional class.
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class Optional<T> {

    /**
     * Creates the new optional from the specified (possibly <code>null</code>) value.
     */
    public static <X> Optional<X> of(X value) {
        return new Optional<X>(value);
    }

    /**
     * Creates a new empty optional.
     */
    public static <X> Optional<X> empty() {
        return new Optional<X>(null);
    }

    T value;

    private Optional(T value) {
        this.value = value;
    }

    /**
     * Returns <code>true</code> if the value is present, ie. not-null.
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Invokes the specified consumer if the optional is present.
     */
    public void ifPresent(Consumer<T> c) {
        if (isPresent()) {
            c.accept(get());
        }
    }

    /**
     * Returns the value or throws {@link NoSuchElementException} if the value does not
     * exist.
     */
    public T get() {
        return get("null value");
    }

    /**
     * Returns the value or throws {@link NoSuchElementException} if the value does not
     * exist.
     * @param errMsg The message to use for the exception if the value is null.
     */
    public T get(String errMsg) {
        if (value == null) {
            throw new NoSuchElementException(errMsg);
        }
        return value;
    }

    /**
     * Converts the value (if set)  to the specified class.
     * <p>
     * This method calls {@link Convert#to(Object, Class)} to do the conversion. If no conversion can be done
     * {@link Optional#empty()} is returned.
     * </p>
     *
     * @param type The type to convert to.
     *
     */
    public <X> Optional<X> to(Class<X> type) {
        if (value == null) {
            return (Optional<X>) this;
        }

        return Convert.to(value, type);
    }

    /**
     * Returns the underlying value if the optional is present, otherwise returns the
     * specified fallback.
     *
     * @param fallback Value to return if this optional is empty.
     */
    public T orElse(T fallback) {
        return value != null ? value : fallback;
    }

    /**
     * Returns the underlying value if the optional is present, otherwise throws the
     * exception supplied by <tt>err</tt>.
     *
     */
    public T orElseThrow(Supplier<RuntimeException> err) {
        if (value != null) {
            return value;
        }

        throw err.get();
    }

    /**
     * Maps the optional to a different type.
     *
     * @param f The mapping function.
     *
     * @return The new optional, or empty if this optoinal is empty.
     */
    public <R> Optional<R> map(Function<T,R> f) {
        if (!isPresent()) {
            return (Optional<R>) this;
        }

        return Optional.of(f.apply(get()));
    }

    /**
     * Applies a predicate to the optional, returning empty if the value doesn't match the
     * predicate.
     *
     * @param filter The predicate to apply to the value.
     */
    public Optional<T> filter(Predicate<T> filter) {
        if (!isPresent()) {
            return this;
        }

        if (filter.test(get())) {
            return this;
        }

        return empty();
    }
}
