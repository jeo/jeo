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

import io.jeo.data.Cursor.Mode;
import io.jeo.data.Dataset;

/**
 * A data set consisting of objects with a vector geometry, known as {@link Feature} objects.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public interface VectorDataset extends Dataset {

    /**
     * The schema for the data set.
     */
    Schema schema() throws IOException;

    /**
     * Counts features in the data set.
     * 
     * @param q Query used to constrain results, must not be <code>null</code>
    */
    long count(VectorQuery q) throws IOException ;

    /**
     * Returns a feature cursor for the data set.
     * <p>
     * {@link VectorQuery#mode()} is used to control whether the cursor is read or write. All
     * implementations must support {@link Mode#READ}.
     * </p>
     * @param q A query used to constrain results, must not be <code>null</code>.
     */
    FeatureCursor cursor(VectorQuery q) throws IOException;

}
