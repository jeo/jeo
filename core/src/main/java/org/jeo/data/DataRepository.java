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
package org.jeo.data;

import java.io.IOException;

import org.jeo.filter.Filter;

/**
 * A repository of data objects. A repository can contain Workspaces and Style
 * objects. For any type of object, the name must be unique in the
 * DataRepository.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public interface DataRepository extends Disposable {

    /**
     * Queries handles present in the repository.
     * @param filter The non-null filter to use for querying
     *
     * @return An Iterable of Handle objects that match the filter
     */
    Iterable<Handle<?>> query(Filter<? super Handle<?>> filter) throws IOException;

    /**
     * Returns a data object object by name.
     *
     * @param name The non-null name of the object.
     * @param type The non-null type of the object, Workspace or Style supported
     * 
     * @return The object or <code>null</code> if so such object matching the name and type exists.
     */
    <T> T get(String name, Class<T> type) throws IOException;

}
