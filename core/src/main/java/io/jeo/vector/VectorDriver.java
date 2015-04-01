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
package io.jeo.vector;

import java.io.IOException;
import java.util.Map;

import io.jeo.data.Driver;
import io.jeo.util.Messages;

/**
 * Base interface for vector drivers.
 */
public interface VectorDriver<T> extends Driver<T> {

    /**
     * Vector driver capability enumeration.
     */
    public static enum Capability {
        /**
         * Ability to handle bounding box queries.
         */
        BOUND,
        /**
         * Ability to handle filters.
         */
        FILTER,
        /**
         * Ability to perform reprojection.
         */
        REPROJECT,
        /**
         * Ability to simplify vector geometries.
         */
        SIMPLIFY,
        /**
         * Ability to limit a result set.
         */
        LIMIT,
        /**
         * Ability to handle offset of a result set.
         */
        OFFSET,
        /**
         * Ability to sort a result set.
         */
        SORT;
    }

    /**
     * Determines if the driver natively supports the specified capability.
     */
    boolean supports(VectorDriver.Capability cap);

    boolean canCreate(Map<?,Object> opts, Messages msgs);

    T create(Map<?,Object> opts, Schema schema) throws IOException;
}
