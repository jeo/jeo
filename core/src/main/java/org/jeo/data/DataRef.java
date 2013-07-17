package org.jeo.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Reference to a data object.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class DataRef<T> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    public static <T> DataRef<T> create(Class<T> type, String path) {
        return create(type, "/", path);
    }

    public static <T> DataRef<T> create(Class<T> type, String sep, String path) {
        return new DataRef<T>(type, path.split(sep));
    }

    Class<T> type;
    String[] path;

    public DataRef(Class<T> type, String... path) {
        this.type = type;
        this.path = path;

        if (path.length == 0) {
            throw new IllegalArgumentException("path must not be empty");
        }
    }

    public Class<T> getType() {
        return type;
    }

    public List<String> getPath() {
        return Arrays.asList(path);
    }

    public String get() {
        if (path.length == 1) {
            return path[0];
        }

        StringBuilder sb = new StringBuilder();
        for (String p : path) {
            sb.append(p).append("/");
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    public String first() {
        return path[0];
    }

    public String last() {
        return path[path.length-1];
    }

    /**
     * Resolves the reference relative to the specified registry.
     * 
     * @param registry The registry to look up the reference in.
     * 
     * @return The data object, or <code>null</code> if it doesn't exist.
     */
    public T resolve(Registry registry) throws IOException {
        Object result = registry.get(first());

        if (result != null && path.length > 1) {
            Workspace ws = (Workspace) result;
            result = ws.get(path[1]); 
        }

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
        return check(workspace.get(last()));
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

    public <S> DataRef<S> append(Class<S> type, String part) {
        String[] arr = new String[path.length+1];
        System.arraycopy(path, 0, arr, 0, path.length);
        arr[arr.length-1] = part;

        return new DataRef<S>(type, arr);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(path);
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
        if (!Arrays.equals(path, other.path))
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
        return type.getSimpleName() + "[" + get() + "]";
    }
}
