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
     * Ability to create datasets.
     */
    Capability CREATE = new Capability("create");
    /**
     * Ability to destroy datasets.
     */
    Capability DESTROY = new Capability("destroy");

    /**
     * Ability to update dataset contents.
     */
    Capability UPDATE = new Capability("update");
    /**
     * Ability to append dataset contents.
     */
    Capability APPEND = new Capability("append");

    /**
     * Ability to handle field selection.
     */
    Capability FIELD = new Capability("field");
    /**
     * Ability to handle bounding box queries.
     */
    Capability BOUND = new Capability("bound");
    /**
     * Ability to handle filters.
     */
    Capability FILTER = new Capability("filter");
    /**
     * Ability to perform reprojection.
     */
    Capability REPROJECT = new Capability("reproject");
    /**
     * Ability to simplify vector geometries.
     */
    Capability SIMPLIFY = new Capability("simplify");
    /**
     * Ability to limit a result set.
     */
    Capability LIMIT = new Capability("limit");
    /**
     * Ability to handle offset of a result set.
     */
    Capability OFFSET = new Capability("offset");
    /**
     * Ability to sort a result set.
     */
    Capability SORT = new Capability("sort");

    /**
     * Determines if this driver can create a connection to the data described by the specified
     * options from scratch.
     * <p>
     * This method differs from {@link #canOpen(Map, Messages)} in that it is meant to operate
     * when a data source physically doesn't exist. It is typically used by file based drivers.
     * </p>
     * <p>
     * The <tt>messages</tt> list is optionally used for the driver to report back any messages
     * or exceptions that prevent the driver from creating the specified data source.
     * </p>
     * @param opts Options describing the data.
     * @param messages Messages reported from the driver, optionally <code>null</code>.
     *
     * @return True if the driver can open the data, otherwise false.
     */
    boolean canCreate(Map<?,Object> opts, Messages messages);

    /**
     * Creates a new data source described by the specified options and schema.
     *
     * @param opts Options describing the data to connect to.
     * @param schema Schema to create.
     *
     * @return The data.
     *
     * @throws IOException In the event of a connection error such as a file system error or
     *   database connection failure.
     */
    T create(Map<?,Object> opts, Schema schema) throws IOException;
}
