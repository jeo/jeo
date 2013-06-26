package org.jeo.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeo.feature.Feature;

/**
 * Filter for matching {@link Feature} objects by id.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Id extends Filter {

    List<Expression> ids;

    public Id(Expression... ids) {
        this(Arrays.asList(ids)); 
    }

    public Id(List<Expression> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("no ids specified");
        }
        this.ids = new ArrayList<Expression>(ids);
    }

    public List<Expression> getIds() {
        return ids;
    }

    @Override
    public boolean apply(Object obj) {
        if (obj instanceof Feature) {
            for (Expression e : ids) {
                Object val = e.evaluate(obj);
                if (val != null && val.toString().equals(((Feature) obj).getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public String toString() {
        return new StringBuilder("ID(").append(ids).append(")").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ids == null) ? 0 : ids.hashCode());
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
        Id other = (Id) obj;
        if (ids == null) {
            if (other.ids != null)
                return false;
        } else if (!ids.equals(other.ids))
            return false;
        return true;
    }

    
}
