package org.jeo.data;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jeo.feature.Schema;
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
    LoadingCache<String, Workspace> wsCache;

    public CachedRepository(DataRepository reg) {
        this(reg, 20);
    }

    public CachedRepository(DataRepository reg, final int cacheSize) {
        this.reg = reg;
        wsCache = CacheBuilder.newBuilder().maximumSize(cacheSize)
            .removalListener(new RemovalListener<String, Workspace>() {
                @Override
                public void onRemoval(RemovalNotification<String, Workspace> n) {
                    n.getValue().close();
                }
            }).build(new CacheLoader<String, Workspace>() {
                @Override
                public Workspace load(String key) throws Exception {
                    return new CachedWorkspace(CachedRepository.this.reg.get(key), cacheSize);
                }
            });
    }

    @Override
    public Iterable<WorkspaceHandle> list() throws IOException {
        //TODO: might want to cache this
        return reg.list();
    }

    @Override
    public Workspace get(String key) throws IOException {
        try {
            return wsCache.get(key);
        } catch (ExecutionException e) {
            LOG.warn("Error loading object from cache", e);
            return reg.get(key);
        }
    }

    @Override
    public void close() {
        wsCache.invalidateAll();
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
        public Iterable<DatasetHandle> list() throws IOException {
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
