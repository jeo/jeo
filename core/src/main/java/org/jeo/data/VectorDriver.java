package org.jeo.data;

import java.io.IOException;
import java.util.Map;

import org.jeo.feature.Schema;

public interface VectorDriver<T extends VectorData> extends Driver<T> {

    boolean canCreate(Map<?,Object> opts);

    T create(Map<?,Object> opts, Schema schema) throws IOException;
}
