package org.jeo.data;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jeo.feature.Schema;
import org.jeo.util.Messages;
import org.jeo.util.Util;

/**
 * Base class for file based vector drivers.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 * @param <T>
 */
public abstract class FileVectorDriver<T> extends FileDriver<T> implements VectorDriver<T> {

    @Override
    public final boolean canCreate(Map<?, Object> opts, Messages msgs) {
        if (!FILE.has(opts)) {
            Messages.of(msgs).report("No " + FILE + " option specified");
            return false;
        }

        File file = file(opts);
        if (file == null) {
            Messages.of(msgs).report("Unable to obtain file from " + FILE.raw(opts));
            return false;
        }

        if (!Util.isEmpty(file)) {
            Messages.of(msgs).report(file.getPath() + " is not empty");
            return false;
        }

        return canCreate(file, opts, msgs);
    }

    protected boolean canCreate(File file, Map<?,Object> opts, Messages msgs) {
        return true;
    }

    @Override
    public final T create(Map<?, Object> opts, Schema schema) throws IOException {
        File file = file(opts);
        return create(file, opts, schema);
    }

    protected abstract T create(File file, Map<?, Object> opts, Schema schema) throws IOException;
}
