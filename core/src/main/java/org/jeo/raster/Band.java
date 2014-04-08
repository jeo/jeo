/* Copyright 2014 The jeo project. All rights reserved.
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
package org.jeo.raster;

import java.io.IOException;

/**
 * A band or component of a raster dataset.
 */
public interface Band {

    /**
     * Enumeration for color interpretation.
     */
    static enum Color {
        UNDEFINED, GRAY, RED, GREEN, BLUE, OTHER;
    }

    /**
     * The name of the band.
     */
    String name();

    /**
     * Returns the numeric type of data stored in the band.
     */
    DataType datatype();

    /**
     * The color interpretation of the band.
     */
    Color color();

    /**
     * The nodata value of the band.
     *
     * @return The value or <tt>null</tt> if the band has no nodata value.
     */
    Double nodata();

    /**
     * Computes statistics of the band.
     *
     * @return The object containing the statistics.
     */
    Stats stats() throws IOException;
}
