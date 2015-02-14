/* Copyright 2015 The jeo project. All rights reserved.
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
package org.jeo.cli.cmd;

import org.jeo.cli.JeoCLI;
import org.jeo.data.Cursor;
import org.jeo.vector.Feature;
import org.jeo.vector.VectorDataset;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstraction for the vector output of a cli command.
 */
public interface VectorSink {

    /**
     * Encodes a cursor.
     *
     * @param cursor The cursor to encode.
     * @param source The dataset, possibly <code>null</code> the cursor originated from.
     * @param cli The cli object.
     *
     */
    void encode(Cursor<Feature> cursor, VectorDataset source, JeoCLI cli) throws IOException;
}
