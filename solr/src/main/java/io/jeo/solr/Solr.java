/* Copyright 2015 The jeo project. All rights reserved.
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
package io.jeo.solr;

import io.jeo.util.Key;
import io.jeo.util.Messages;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDriver;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static io.jeo.vector.VectorDriver.Capability.BOUND;
import static io.jeo.vector.VectorDriver.Capability.FIELD;
import static io.jeo.vector.VectorDriver.Capability.FILTER;
import static io.jeo.vector.VectorDriver.Capability.LIMIT;
import static io.jeo.vector.VectorDriver.Capability.OFFSET;

/**
 * Driver for Apache Solr.
 */
public class Solr implements VectorDriver<SolrWorkspace> {

    public static final Key<URL> URL = new Key<>("url", URL.class);

    public static SolrWorkspace open(String url) throws IOException {
        return new Solr().open((Map) Collections.singletonMap(URL, url));
    }

    @Override
    public String name() {
        return "Solr";
    }

    @Override
    public List<String> aliases() {
        return null;
    }

    @Override
    public boolean isEnabled(Messages messages) {
        return true;
    }

    @Override
    public Class<SolrWorkspace> type() {
        return SolrWorkspace.class;
    }

    @Override
    public String family() {
        return "jeo";
    }

    @Override
    public List<Key<?>> keys() {
        return (List) Arrays.asList(URL);
    }

    @Override
    public boolean canOpen(Map<?, Object> opts, Messages messages) {
        if (!URL.in(opts)) {
            Messages.of(messages).report("No " + URL + " option specified");
            return false;
        }

        return true;
    }

    @Override
    public SolrWorkspace open(Map<?, Object> opts) throws IOException {
        URL url = URL.get(opts);
        return new SolrWorkspace(new HttpSolrClient(url.toString()));
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        return false;
    }

    @Override
    public SolrWorkspace create(Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    static EnumSet<Capability> CAPABILITIES = EnumSet.of(BOUND, FILTER, FIELD, LIMIT, OFFSET);

    @Override
    public boolean supports(Capability cap) {
        return CAPABILITIES.contains(cap);
    }
}
