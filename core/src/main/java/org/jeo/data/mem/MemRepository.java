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
import org.jeo.util.Pair;
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
    Map<Pair<String, Class>, Object> map;

    /**
     * Constructs a new empty repo.
     */
    public MemRepository() {
        map = new LinkedHashMap<Pair<String, Class>, Object>();
    }

    @Override
    public Iterable<Handle<?>> query(Filter<? super Handle<?>> filter) {
        List<Handle<?>> list = new ArrayList<Handle<?>>();
        for (Map.Entry<Pair<String, Class>, Object> kv : map.entrySet()) {
            final Object obj = kv.getValue();

            //TODO: driver
            Handle<Object> h = new Handle<Object>(kv.getKey().first(), obj.getClass(), null) {
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
    public Object get(String name, Class type) throws IOException {
        return map.get(new Pair(name, type));
    }

    /**
     * Adds a object to the repository.
     * 
     * @param key The name/key of the workspace.
     * @param obj The object.
     */
    public void put(String key, Object obj) {
        map.put(new Pair(key, obj.getClass()), obj);
    }

    @Override
    public void close() {
        for (Object obj : map.values()) {
            if (obj instanceof Disposable) {
                ((Disposable) obj).close();
            }
        }
        map.clear();
    }
}
