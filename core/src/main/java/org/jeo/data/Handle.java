package org.jeo.data;

import java.io.IOException;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

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
     * object description
     */
    protected String description;

    /**
     * object coordinate reference system
     */
    protected CoordinateReferenceSystem crs;

    /**
     * object bounding box
     */
    protected Envelope bounds;

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
    @SuppressWarnings("unchecked")
    protected Handle(String name, @SuppressWarnings("rawtypes") Class type, Driver<?> driver) {
        this.name = name;
        this.type = type;
        this.driver = driver;
    }

    protected Handle(String name, Driver<?> driver) {
        this(name, driver.getType(), driver);
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
     * @throws IOException I/O errors that occur interacting with the underlying data object.
     */
    public String title() throws IOException {
        if (title == null) {
            if (Dataset.class.isAssignableFrom(type)) {
                Dataset data = (Dataset) resolve();
                title = data.getTitle();
            }
        }
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
     * @throws IOException I/O errors that occur interacting with the underlying data object. 
     */
    public String description() throws IOException {
        if (description == null) {
            if (Dataset.class.isAssignableFrom(type)) {
                description = ((Dataset) resolve()).getDescription();
            }
        }
        return description;
    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public CoordinateReferenceSystem crs() throws IOException {
        if (crs == null ) {
            if (Dataset.class.isAssignableFrom(type)) {
                crs = ((Dataset) resolve()).crs();
            }
        }
        return crs;
    }

    public Envelope getBounds() {
        return bounds;
    }

    public void setBounds(Envelope bounds) {
        this.bounds = bounds;
    }

    public Envelope bounds() throws IOException {
        if (bounds == null) {
            bounds = ((Dataset)resolve()).bounds();
        }
        return bounds;
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
            try {
                obj = doResolve();
            }
            catch(ClassCastException e) {
                throw new IOException("handle wrong type, expected: " + type.getName(), e);
            }
        }
        return obj;
    }

    /**
     * Create a Handle that implements Handle.doResolve by calling
     * DataRepository.get
     */
    public static <T> Handle<T> to(String name, Driver<?> driver,
            final DataRepository repo) {
        return new Handle<T>(name, driver) {
            @Override
            protected T doResolve() throws IOException {
                return repo.get(name, type);
            }
        };
    }

    /**
     * Create a Handle that implements Handle.doResolve by calling
     * Workspace.get
     */
    public static Handle<Dataset> to(String name, final Workspace workspace) {
        return new Handle<Dataset>(name, Dataset.class, workspace.getDriver()) {
            @Override
            protected Dataset doResolve() throws IOException {
                return workspace.get(name);
            }
        };
    }

    public static Handle<Dataset> to(final Dataset d) {
        return new Handle<Dataset>(d.getName(), d.getClass(), d.getDriver()) {
            @Override
            protected Dataset doResolve() throws IOException {
                return d;
            }
        };
    }

    /**
     * Subclass hook to perform the resolving of the data object.
     * 
     * @return The resolved object.
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
