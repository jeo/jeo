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
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.jeo.data.Dataset;
import io.jeo.data.Disposable;
import io.jeo.data.Driver;
import io.jeo.data.Handle;
import io.jeo.data.Workspace;
import io.jeo.vector.Schema;
import io.jeo.filter.Filter;
import io.jeo.util.Key;
import io.jeo.vector.VectorDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import io.jeo.util.Pair;

public class CachedRepository implements DataRepository {

    static Logger LOG = LoggerFactory.getLogger(CachedRepository.class);

    DataRepository reg;
    LoadingCache<Pair<String, Class>, Object> objCache;

    public CachedRepository(DataRepository reg) {
        this(reg, 20);
    }

    public CachedRepository(DataRepository reg, final int cacheSize) {
        this.reg = reg;
        objCache = CacheBuilder.newBuilder().maximumSize(cacheSize)
            .removalListener(new RemovalListener<Pair<String, Class>, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Pair<String, Class>, Object> n) {
                    Object obj = n.getValue();
                    if (obj instanceof Disposable) {
                        ((Disposable) obj).close();
                    }
                }
            }).build(new CacheLoader<Pair<String, Class>, Object>() {
                @Override
                public Object load(Pair<String, Class> key) throws Exception {
                    Object obj = CachedRepository.this.reg.get(key.first, key.second);
                    if (obj instanceof Workspace) {
                        return new CachedWorkspace((Workspace)obj, cacheSize);
                    }
                    return obj;
                }
            });
    }

    public Iterable<Handle<?>> query(Filter<? super Handle<?>> filter)
            throws IOException {
        //TODO: check for FIlter.all and cache it
        return reg.query(filter);
    }

    @Override
    public <T> T get(String key, Class<T> type) throws IOException {
        try {
            return type.cast(objCache.get(new Pair(key, type)));
        } catch (ExecutionException e) {
            LOG.warn("Error loading object from cache", e);
            return reg.get(key, type);
        }
    }

    @Override
    public void close() {
        objCache.invalidateAll();
        reg.close();
    }

    static class CachedWorkspace implements Workspace {

        Workspace ws;
        LoadingCache<String,Dataset> layerCache;

        CachedWorkspace(Workspace ws, int cacheSize) {
            this.ws = ws;
            layerCache = CacheBuilder.newBuilder().maximumSize(cacheSize)
                .build(new CacheLoader<String, Dataset>() {
                    @Override
                    public Dataset load(String key) throws Exception {
                        return CachedWorkspace.this.ws.get(key);
                    }
            });
        }

        @Override
        public Driver<?> driver() {
            return ws.driver();
        }

        @Override
        public Map<Key<?>, Object> driverOptions() {
            return ws.driverOptions();
        }

        @Override
        public VectorDataset create(Schema schema) throws IOException {
            return ws.create(schema);
        }

        @Override
        public Iterable<Handle<Dataset>> list() throws IOException {
            return ws.list();
        }

        @Override
        public Dataset get(String layer) throws IOException {
            try {
                return layerCache.get(layer);
            } catch (ExecutionException e) {
                LOG.warn("Error loading layer from cache", e);
                return ws.get(layer);
            }
        }

        @Override
        public void close() {
            //do nothing, we wait for the entry to expire before disposing
        }
    }
}
