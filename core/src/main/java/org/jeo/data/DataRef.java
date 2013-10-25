package org.jeo.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference to a data object.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class DataRef<T> implements Serializable, Disposable {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    /** logger */
    static Logger LOG = LoggerFactory.getLogger(DataRef.class);

    /**
     * type of reference
     */
    Class<T> type;

    /**
     * driver for the data
     */
    Driver<?> driver;

    /**
     * object name
     */
    String name;

    /**
     * parent object
     */
    Object parent;

    /**
     * resources to close
     */
    List<Object> close = new ArrayList<Object>();

    public DataRef(String name, Class<T> type, Driver<?> driver, Object parent) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }

        this.name = name;
        this.type = type;
        this.driver = driver;
        this.parent = parent;
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

    /**
     * Resolves the reference.
     * <p>
     * Client code that calls this method must be sure to call {@link #close()}. 
     * </p>
     * 
     * @param registry The registry to look up the reference in.
     * 
     * @return The data object, or <code>null</code> if it doesn't exist.
     */
    public T resolve() throws IOException {
        return resolve(parent);
    }

    public T resolve(Object parent) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("reference has no parent");
        }

        // check if the parent itself is a reference
        Object p = parent;
        if (p instanceof DataRef) {
            // resolve the parent reference
            p = ((DataRef) parent).resolve();
            if (p != null) {
                // keep the object around to close it
                close.add(p);
            }
        }

        Object r = null;
        if (p instanceof Registry) {
            r = check(((Registry) p).get(name));
        }
        else if (p instanceof Workspace) {
            r = check(((Workspace) p).get(name));
        }

        if (r == null) { 
            throw new IllegalArgumentException(
                    "Unable to resolve reference " + name + " relative to: " + p);
        }

        close.add(0, r);
        return type.cast(r);
    }

    public void close() {
        for (Object obj : close) {
            if (obj instanceof Disposable) {
                ((Disposable) obj).close();
            }
            else {
                LOG.debug("Unable to close object: " + obj);
            }
        }
    }

    T check(Object obj) {
        if (obj == null) {
            //ok
            return null;
        }

        // check type
        if (!type.isInstance(obj)) {
            throw new IllegalStateException(String.format("path resolved to %s, espected %s", 
                type.getSimpleName(), obj.getClass().getSimpleName() )); 
        }

        return type.cast(obj);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((driver == null) ? 0 : driver.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataRef other = (DataRef) obj;
        if (driver == null) {
            if (other.driver != null)
                return false;
        } else if (!driver.getClass().equals(other.driver.getClass()))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[%s,%s]", name, type != null ? type.getSimpleName() : "?", 
            driver != null ? driver.getName() : "?");
    }
}
