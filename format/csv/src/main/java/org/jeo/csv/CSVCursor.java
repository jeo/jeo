package org.jeo.csv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.jeo.data.Cursor;
import org.jeo.feature.Feature;

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
