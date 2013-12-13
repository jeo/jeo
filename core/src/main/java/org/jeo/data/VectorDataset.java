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

import org.jeo.feature.Feature;
import org.jeo.feature.Schema;

/**
 * A layer consisting of vector geometry objects, or {@link Feature} objects. 
 *
 * @author Justin Deoliveira, OpenGeo
 */
public interface VectorDataset extends Dataset {

    /**
     * The schema for the layer.
     */
    Schema schema() throws IOException;

    /**
     * Counts features in the layer.
     * 
     * @param q Query used to constrain results, must not be <code>null</code>
    */
    long count(Query q) throws IOException ;

    /**
     * Returns a feature cursor for the layer. 
     * <p>
     * {@link Query#getMode()} is used to control whether the cursor is read or write. All 
     * implementations must support {@link Cursor.Mode#READ}.
     * </p>
     * @param q A query used to constrain results, must not be <code>null</code>.
     */
    Cursor<Feature> cursor(Query q) throws IOException;

}
