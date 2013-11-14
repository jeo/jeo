package org.jeo.data;

import java.io.IOException;

/**
 * Reference to a data object.
 * <p> 
 * A handle is a lightweight proxy to a data object meant to convey information about the object 
 * without necessarily having to load it. The {@link #resolve()} method is used to obtain the 
 * underlying data object from the handle.
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

    /**
     * Creates a new handle.
     * 
     * @param name Name of the object.
     * @param type The type of the data object.
     * @param driver The format driver for the data type.
     */
    protected Handle(String name, Class<T> type, Driver<?> driver) {
        this.name = name;
        this.type = type;
        this.driver = driver;
    }

    /**
     * The name of the data object.
     */
    public String getName() {
        return name;
    }

    /**
     * The type of the data object.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * The format driver for the data type.
     */
    public Driver<?> getDriver() {
        return driver;
    }

    /**
     * Title of the data object.
     * <p>
     * This value may be <code>null</code>. Application code should use {@link #title()} to return
     * a value that may require resolving the underlying data object. 
     * </p>
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the data object.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the title of the data object resolving the handle if necessary.
     *
     * @return The title, or <code>null</code>.
     * 
     * @throws IOException I/O errors that occur interacting with the underlyign data object.
     */
    public String title() throws IOException {
        return title;
    }

    /**
     * Description of the data object.
     * <p>
     * This value may be <code>null</code>. Application code should use {@link #description()()} to 
     * return a value that may require resolving the underlying data object. 
     * </p>
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the data object.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the data object resolving the handle if necessary.
     *
     * @return The description, or <code>null</code>.
     * 
     * @throws IOException I/O errors that occur interacting with the underlyign data object. 
     */
    public String description() throws IOException {
        return description;
    }

    /**
     * Resolves the handle returning the underlying data object.
     * 
     * @return The data object.
     * 
     * @throws IOException I/O errors that occur resolving the data object.
     */
    public T resolve() throws IOException {
        if (obj == null) {
            obj = doResolve();
        }
        return obj;
    }

    /**
     * Subclass hook to perform the resolving of the data object.
     * 
     * @return
     * @throws IOException
     */
    protected abstract T doResolve() throws IOException;

    @Override
    public void close() {
        if (obj != null && (obj instanceof Disposable)) {
            ((Disposable)obj).close();
        }
        obj = null;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", type.getSimpleName(), name);
    }
}
