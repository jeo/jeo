package org.jeo.ogr;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.data.FileVectorDriver;
import org.jeo.feature.Schema;
import org.jeo.util.Messages;

public abstract class OGRDriver<T> extends FileVectorDriver<T> {

    @Override
    protected boolean canCreate(File file, Map<?, Object> opts, Messages msgs) {
        return false;
    }

    @Override
    protected T create(File file, Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean canOpen(File file, Map<?, Object> opts, Messages msgs) {
        Map mopts = new LinkedHashMap(opts);
        mopts.put(OGR.DRIVER, getOGRDriverName());

        return new OGR().canOpen(mopts, msgs);
    }

    @Override
    protected T open(File file, Map<?, Object> opts) throws IOException {
        return open(new OGR().open(opts));
    }

    protected abstract T open(OGRWorkspace workspace) throws IOException;

    protected abstract String getOGRDriverName();
}
