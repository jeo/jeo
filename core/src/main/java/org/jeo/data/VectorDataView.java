package org.jeo.data;

import java.io.IOException;

import org.jeo.feature.Feature;
import org.jeo.filter.Filter;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;

public class VectorDataView {

    VectorData data;

    public VectorDataView(VectorData data) {
        this.data = data;
    }

    public long count() throws IOException {
        return data.count(new Query());
    }

    public Feature first() throws IOException {
        return find(new Query());
    }

    public Feature find(String cql) throws IOException, ParseException {
        return find(CQL.parse(cql));
    }

    public Feature find(Filter filter) throws IOException {
        return find(new Query().filter(filter));
    }

    public Feature find(Query query) throws IOException {
        Cursor<Feature> c = data.cursor(query.limit(1));
        try {
            if (c.hasNext()) {
                return c.next();
            }
        }
        finally {
            c.close();
        }
        return null;
    }
}
