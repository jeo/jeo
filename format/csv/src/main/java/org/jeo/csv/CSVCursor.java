package org.jeo.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;

public class CSVCursor extends Cursor<Feature> {

    CSVDataset csv;
    BufferedReader reader;
    String next;
    int i;

    public CSVCursor(BufferedReader reader, CSVDataset csv) throws FileNotFoundException {
        this.reader = reader;
        this.csv = csv;
        next = null;
        i = 0;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next == null) {
            next = reader.readLine();
        }

        return next != null;
    }

    @Override
    public Feature next() throws IOException {
        if (next == null) {
            return null;
        }

        try {
            return csv.feature(i++, next);
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
