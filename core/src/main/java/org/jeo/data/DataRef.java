package org.jeo.data;

import java.io.IOException;
import java.io.Serializable;

/**
 * Reference to a data object.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class DataRef<T> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    Class<T> type;
    Driver<? super T> driver;
    String name;

    public DataRef(String name, Class<T> type, Driver<? super T> driver) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }

        this.name = name;
        this.type = type;
        this.driver = driver;
    }

    public DataRef(String name, Class<T> type) {
        this(name, type, null);
    }

    public DataRef(String name, Driver<T> driver) {
        this(name, driver != null ? driver.getType() : null, driver);
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public Driver<? super T> getDriver() {
        return driver;
    }

    /**
     * Resolves the reference relative to the specified registry.
     * 
     * @param registry The registry to look up the reference in.
     * 
     * @return The data object, or <code>null</code> if it doesn't exist.
     */
    public T resolve(Registry registry) throws IOException {
        Object result = registry.get(name);
        return check(result);
    }

    /**
     * Resolves the reference relative to the specified workspace.
     * 
     * @param workspace The workspace to look up the reference in.
     * 
     * @return The data object, or <code>null</code> if it doesn't exist.
     */
    public T resolve(Workspace workspace) throws IOException {
        return check(workspace.get(name));
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
        } else if (!driver.equals(other.driver))
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
