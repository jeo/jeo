package org.jeo.csv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeo.data.FileVectorDriver;
import org.jeo.feature.Schema;
import org.jeo.util.Key;
import org.jeo.util.Messages;

public class CSV extends FileVectorDriver<CSVDataset> {

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
    protected boolean canOpen(File file, Map<?,Object> opts, Messages msgs) {
        if (!super.canOpen(file, opts, msgs)) {
            return false;
        }

        if (!file.isFile()) {
            msgs.report(file.getPath() + " is not a file");
            return false;
        }
        return true;
    }

    @Override
    public CSVDataset open(File file, Map<?,Object> opts) throws IOException {
        return new CSVDataset(file, CSVOpts.fromMap(opts));
    }

    @Override
    protected boolean canCreate(File file, Map<?, Object> opts, Messages msgs) {
        //TODO: implement
        Messages.of(msgs).report("Creation unsupported");
        return false;
    }

    @Override
    protected CSVDataset create(File file, Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }
}
