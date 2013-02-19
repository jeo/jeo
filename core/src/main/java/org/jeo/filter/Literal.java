package org.jeo.filter;

/**
 * Static expression that evaluates to a literal value.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Literal<T> implements Expression<T> {

    T value;

    public Literal(T value) {
        this.value = value;
    }

    @Override
    public T evaluate(Object obj) {
        return value;
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        Literal<?> other = (Literal<?>) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
