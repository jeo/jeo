package org.jeo.data;

import java.io.IOException;
import java.util.Map;

import org.jeo.feature.Schema;
import org.jeo.util.Messages;

public interface VectorDriver<T> extends Driver<T> {

    boolean canCreate(Map<?,Object> opts, Messages msgs);

    T create(Map<?,Object> opts, Schema schema) throws IOException;
}
