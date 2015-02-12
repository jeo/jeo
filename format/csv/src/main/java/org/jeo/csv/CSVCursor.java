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
package org.jeo.csv;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.vector.Feature;

import com.csvreader.CsvReader;

public class CSVCursor extends Cursor<Feature> {

    CSVDataset csv;
    CsvReader reader;
    Feature next;
    int i;

    public CSVCursor(CsvReader reader, CSVDataset csv) throws FileNotFoundException {
        this.reader = reader;
        this.csv = csv;
        next = null;
        i = 0;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null && reader.readRecord()) {
            next = csv.feature(i++, reader);
        }

        return next != null;
    }

    @Override
    public Feature next() throws IOException {
        try {
            return next;
        }
        finally {
            next = null;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
