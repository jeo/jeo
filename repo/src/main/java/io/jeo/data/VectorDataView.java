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
package io.jeo.data;

import java.io.IOException;

import io.jeo.Pending;
import io.jeo.vector.Feature;
import io.jeo.filter.Filter;
import io.jeo.filter.cql.CQL;
import io.jeo.filter.cql.ParseException;
import io.jeo.vector.VectorQuery;
import io.jeo.vector.VectorDataset;

@Pending
public class VectorDataView {

    VectorDataset data;

    public VectorDataView(VectorDataset data) {
        this.data = data;
    }

    public long count() throws IOException {
        return data.count(new VectorQuery());
    }

    public Feature first() throws IOException {
        return find(new VectorQuery());
    }

    public Feature find(String cql) throws IOException, ParseException {
        return find(CQL.parse(cql));
    }

    public Feature find(Filter filter) throws IOException {
        return find(new VectorQuery().filter(filter));
    }

    public Feature find(VectorQuery query) throws IOException {
        Cursor<Feature> c = data.read(query.limit(1));
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
