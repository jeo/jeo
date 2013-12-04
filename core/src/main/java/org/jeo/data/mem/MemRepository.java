package org.jeo.data.mem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeo.data.DataRepository;
import org.jeo.data.Disposable;
import org.jeo.data.Handle;
import org.jeo.data.Workspace;
import org.jeo.filter.Filter;
import org.jeo.filter.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A repository that stores workspace objects in a {@link Map} in memory.
 * <p>
 * The {@link #put(String, Workspace)} method is used to register workspaces.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class MemRepository implements DataRepository {

    /** logger */
    static Logger LOG = LoggerFactory.getLogger(MemRepository.class);

    /** registry map */
    Map<String, Object> map;

    /**
     * Constructs a new empty repo.
     */
    public MemRepository() {
        map = new LinkedHashMap<String, Object>();
    }

    @Override
    public Iterable<Handle<?>> query(Filter<? super Handle<?>> filter) {
        List<Handle<?>> list = new ArrayList<Handle<?>>();
        for (Map.Entry<String, Object> kv : map.entrySet()) {
            final Object obj = kv.getValue();

            //TODO: driver
            Handle<Object> h = new Handle<Object>(kv.getKey(), obj.getClass(), null) {
                @Override
                protected Object doResolve() throws IOException {
                    return obj;
                }
            };
            if (filter.apply(h)) {
                list.add(h);
            }
        }
        return list;
    }

    @Override
    public Iterable<Handle<?>> list() {
        return query(Filters.all());
    }

    @Override
    public Object get(String name) throws IOException {
        return map.get(name);
    }

    /**
     * Adds a object to the repository.
     * 
     * @param key The name/key of the workspace.
     * @param obj The object.
     */
    public void put(String key, Object obj) {
        map.put(key, obj);
    }

    public void close() {
        for (Object obj : map.values()) {
            if (obj instanceof Disposable) {
                ((Disposable) obj).close();
            }
        }
        map.clear();
    }
}
