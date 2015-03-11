/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.filter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Filter that applies a logical operator to a set of expressions.   
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Logic<T> extends Filter<T> {
    
    /**
     * Logical operator type.  
     */
    public enum Type {
        AND, OR, NOT;
    }

    Type type;
    List<Filter<T>> parts;

    public Logic(Type type, Filter<T>... parts) {
        this(type, Arrays.asList(parts));
    }
    
    public Logic(Type type, List<Filter<T>> parts) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(parts, "parts must not be null");
        this.type = type;
        this.parts = parts;

        if (parts.isEmpty()) {
            throw new IllegalArgumentException("Logic filter must contain > 0 operands");
        }
        if (type == Type.NOT && parts.size() != 1) {
            throw new IllegalArgumentException("Not filter must contain exactly 1 operands"); 
        }
    }

    public Type type() {
        return type;
    }

    public List<Filter<T>> parts() {
        return parts;
    }

    @Override
    public boolean test(T obj) {
        switch(type) {
        case AND:
            return and(obj);
        case OR:
            return or(obj);
        case NOT: 
            return not(obj);
        default:
            throw new IllegalStateException();
        }
    }

    boolean not(T obj) {
        return !parts.get(0).test(obj);
    }

    boolean and(T obj) {
        Iterator<Filter<T>> it = parts.iterator();
        boolean result = it.next().test(obj);
        while (it.hasNext()) {
            result =  it.next().test(obj) && result;
        }
        return result;
    }

    boolean or(T obj) {
        Iterator<Filter<T>> it = parts.iterator();
        boolean result = it.next().test(obj);
        while (it.hasNext() && !result) {
            result = result || it.next().test(obj);
        }
        return result;
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parts == null) ? 0 : parts.hashCode());
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
        Logic<?> other = (Logic<?>) obj;
        if (parts == null) {
            if (other.parts != null)
                return false;
        } else if (!parts.equals(other.parts))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (type == Type.NOT) {
            return "NOT " + parts.get(0).toString();
        }
        else {
            StringBuilder sb = new StringBuilder();
            for (Filter<T> f : parts) {
                sb.append(f).append(" ").append(type.name()).append(" ");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length()-type.name().length()-2);
            }
            return sb.toString();
        }
    }
}
