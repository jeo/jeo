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

import java.util.Objects;

/**
 * Base class for filters that involve a left and a right expression.
 */
public abstract class BinaryFilter<T> extends Filter<T> {

    protected Expression left;
    protected Expression right;

    protected BinaryFilter(Expression left, Expression right) {
        Objects.requireNonNull(left, "operands must not be null");
        Objects.requireNonNull(right, "right operand must not be null");

        this.left = left;
        this.right = right;
    }

    /**
     * The left expression.
     */
    public Expression left() {
        return left;
    }

    /**
     * The right expression.
     */
    public Expression right() {
        return right;
    }


    /**
     * Normalizes the filter.
     * <p>
     * A "normal" filter is one where one of the left or the right of the filter is a {@link Property}.
     * This method returns <code>null</code> if the filter is not normal.
     * </p>
     */
    public BinaryFilter<T> normalize() {
        if (left instanceof Property) {
            return this;
        }
        else if (right instanceof Property) {
            return invert();
        }
        else {
            return null;
        }
    }

    /**
     * Returns the {@link Property} of a normal binary filter.
     *
     * @see #normalize()
     */
    public Property property() {
        if (left instanceof Property) {
            return (Property) left;
        }
        else if (right instanceof Property) {
            return (Property) right;
        }
        else {
            return null;
        }
    }

    /**
     * Inverts the filter, swapping the left and right arguments.
     */
    public abstract BinaryFilter<T> invert();
}
