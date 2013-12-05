package org.jeo.data;

import java.io.IOException;
import org.jeo.filter.Filter;
import org.jeo.filter.Filters;

public class DataRepositoryView implements DataRepository {
    
    private final DataRepository repo;

    public DataRepositoryView(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Get all handles for objects in the repository. This includes
     * workspaces, their datasets and any styles.
     *
     * @return An Iterable of Handle objects for all contents.
     */
    public Iterable<Handle<?>> list() throws IOException {
        return repo.query(Filters.all());
    }

    @Override
    public Iterable<Handle<?>> query(Filter<? super Handle<?>> filter) throws IOException {
        return repo.query(filter);
    }

    @Override
    public <T> T get(String name, Class<T> type) throws IOException {
        return repo.get(name, type);
    }

    @Override
    public void close() {
        repo.close();
    }

}
