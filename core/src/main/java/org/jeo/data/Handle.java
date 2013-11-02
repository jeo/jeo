package org.jeo.data;

import java.io.IOException;

/**
 * Reference to a data object.
 * <p> 
 * A handle is a lightweight proxy to a data object meant to convey information about the object 
 * without necessarily having to load it.
 * </p>
 * 
 * @author Justin Deoliveira, Boundless
 *
 * @param <T>
 */
public abstract class Handle<T> implements Disposable {

    /**
     * object name
     */
    protected String name;

    /**
     * object title
     */
    protected String title;

    /**
     * object title
     */
    protected String description;

    /**
     * object type
     */
    protected Class<T> type;

    /**
     * object driver
     */
    protected Driver<?> driver;

    /**
     * The live object.
     */
    protected T obj;

    protected Handle(String name, Class<T> type, Driver<?> driver) {
        this.name = name;
        this.driver = driver;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public Driver<?> getDriver() {
        return driver;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String title() throws IOException {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String description() throws IOException {
        return description;
    }

    public T resolve() throws IOException {
        if (obj == null) {
            obj = doResolve();
        }
        return obj;
    }

    protected abstract T doResolve() throws IOException;

    @Override
    public void close() {
        if (obj != null && (obj instanceof Disposable)) {
            ((Disposable)obj).close();
        }
        obj = null;
    }
}
