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
package io.jeo.geotools;

import io.jeo.util.Function;
import io.jeo.util.Key;
import io.jeo.util.Messages;
import io.jeo.util.Optional;
import io.jeo.vector.Schema;
import io.jeo.vector.VectorDriver;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.jeo.util.Util.set;

/**
 * Abstract base class for GeoTools based drivers.
 */
public abstract class GTVectorDriver implements VectorDriver<GTWorkspace> {

    String factoryClassName;
    volatile DataStoreFactorySpi factory;

    public GTVectorDriver(String factoryClassName) {
        this.factoryClassName = factoryClassName;
    }

    @Override
    public String name() {
        return factory(null).map(new Function<DataStoreFactorySpi, String>() {
            @Override
            public String apply(DataStoreFactorySpi f) {
                return f.getDisplayName();
            }
        }).orElse("GeoTools");
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public Class<GTWorkspace> type() {
        return GTWorkspace.class;
    }

    @Override
    public List<Key<?>> keys() {
        DataStoreFactorySpi factory = factory(null).get();

        List<Key<?>> keys = new ArrayList<>();
        for (Param p : factory.getParametersInfo()) {
            keys.add(new Key(p.getName(), p.getType(), p.getDefaultValue()));
        }

        return keys;
    }

    @Override
    public String family() {
        return "geotools";
    }

    @Override
    public boolean isEnabled(Messages messages) {
        return factory(messages).map(new Function<DataStoreFactorySpi, Boolean>() {
            @Override
            public Boolean apply(DataStoreFactorySpi f) {
                return f.isAvailable();
            }
        }).orElse(false);
    }

    //TODO: others are supported but we haven't mapped them over yet
    static Set<Capability> CAPABILITIES = set(BOUND, FILTER, LIMIT, OFFSET);

    @Override
    public Set<Capability> capabilities() {
        return CAPABILITIES;
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        return false;
    }

    @Override
    public GTWorkspace create(Map<?, Object> opts, Schema schema) throws IOException {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canOpen(Map<?, Object> opts, Messages messages) {
        return factory(messages).get().canProcess(params(opts));
    }

    @Override
    public GTWorkspace open(Map<?, Object> opts) throws IOException {
        DataStore store = factory(null).get().createDataStore(params(opts));
        return new GTWorkspace(store, this);
    }

    Map params(Map<?, Object> opts) {
        Map params = new HashMap<>();
        for (Key<?> k : keys()) {
            params.put(k.name(), k.get(opts));
        }
        return params;
    }

    Optional<DataStoreFactorySpi> factory(Messages msgs) {
        msgs = Messages.of(msgs);
        if (factory == null) {
            synchronized (this) {
                if (factory == null) {
                    Class<DataStoreFactorySpi> factoryClass;
                    try {
                        factoryClass = (Class<DataStoreFactorySpi>) Class.forName(factoryClassName);
                        factory = factoryClass.newInstance();
                    } catch (Exception e) {
                        msgs.report(e);
                    }
                }
            }
        }

        return Optional.of(factory);
    }
}
