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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeo.vector.Feature;

/**
 * Filter for matching {@link Feature} objects by id.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Id<T> extends Filter<T> {

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

    public List<Expression> ids() {
        return ids;
    }

    @Override
    public boolean test(T obj) {
        if (obj instanceof Feature) {
            for (Expression e : ids) {
                Object val = e.evaluate(obj);
                if (val != null && val.toString().equals(((Feature) obj).id())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <R> R accept(FilterVisitor<R> v, Object obj) {
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
        Id<?> other = (Id<?>) obj;
        if (ids == null) {
            if (other.ids != null)
                return false;
        } else if (!ids.equals(other.ids))
            return false;
        return true;
    }

    
}
