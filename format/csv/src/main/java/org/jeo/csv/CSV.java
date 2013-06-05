package org.jeo.csv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileDriver;
import org.jeo.util.Key;

public class CSV extends FileDriver<CSVDataset> {

    public static final Key<Delimiter> DELIM = 
        new Key<Delimiter>("delim", Delimiter.class, Delimiter.comma());

    public static final Key<Boolean> HEADER = new Key<Boolean>("header", Boolean.class, true);

    public static final Key<Object> X = new Key<Object>("x", Object.class, "x");

    public static final Key<Object> Y = new Key<Object>("y", Object.class, "y");

    public static CSVDataset open(File file, CSVOpts csvOpts) throws IOException {
        return new CSVDataset(file, csvOpts);
    }

    @Override
    public String getName() {
        return "CSV";
    }

    @Override
    public List<Key<?>> getKeys() {
        return (List) Arrays.asList(FILE, DELIM, HEADER, X, Y);
    }

    @Override
    public Class<CSVDataset> getType() {
        return CSVDataset.class;
    }

    @Override
    public boolean canOpen(File file, Map<?,Object> opts) {
        return super.canOpen(file, opts) && file.isFile();
    }

    @Override
    public CSVDataset open(File file, Map<?,Object> opts) throws IOException {
        return new CSVDataset(file, csvOpts(opts));
    }

    CSVOpts csvOpts(Map<?, Object> opts) {
        CSVOpts csvOpts = new CSVOpts();
        csvOpts.delimiter(DELIM.get(opts)).header(HEADER.get(opts));
        
        Object x = X.get(opts);
        if (x instanceof Integer) {
            csvOpts.xy((Integer)x, (Integer)Y.get(opts));
        }
        else {
            csvOpts.xy(x.toString(), Y.get(opts).toString());
        }

        return csvOpts;
    }
}
