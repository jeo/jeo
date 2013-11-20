package org.jeo.data;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jeo.feature.Schema;
import org.jeo.filter.Filter;
import org.jeo.util.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class CachedRepository implements DataRepository {

    static Logger LOG = LoggerFactory.getLogger(CachedRepository.class);

    DataRepository reg;
    LoadingCache<String, Object> objCache;

    public CachedRepository(DataRepository reg) {
        this(reg, 20);
    }

    public CachedRepository(DataRepository reg, final int cacheSize) {
        this.reg = reg;
        objCache = CacheBuilder.newBuilder().maximumSize(cacheSize)
            .removalListener(new RemovalListener<String, Object>() {
                @Override
                public void onRemoval(RemovalNotification<String, Object> n) {
                    Object obj = n.getValue();
                    if (obj instanceof Disposable) {
                        ((Disposable)obj).close();
                    }
                }
            }).build(new CacheLoader<String, Object>() {
                @Override
                public Object load(String key) throws Exception {
                    Object obj = CachedRepository.this.reg.get(key);
                    if (obj instanceof Workspace) {
                        return new CachedWorkspace((Workspace)obj, cacheSize);
                    }
                    
                    return obj;
                }
            });
    }

    @Override
    public Iterable<Handle<Object>> query(Filter<? super Handle<Object>> filter)
            throws IOException {
        //TODO: check for FIlter.all and cache it
        return reg.query(filter);
    }

    @Override
    public Object get(String key) throws IOException {
        try {
            return objCache.get(key);
        } catch (ExecutionException e) {
            LOG.warn("Error loading object from cache", e);
            return reg.get(key);
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
        public Driver<?> getDriver() {
            return ws.getDriver();
        }

        @Override
        public Map<Key<?>, Object> getDriverOptions() {
            return ws.getDriverOptions();
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
