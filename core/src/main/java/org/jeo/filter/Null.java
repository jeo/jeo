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

/**
 * Implementation of IS NULL and IS NOT NULL.
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class Null<T> extends Filter<T> {

    final boolean not;
    final Property prop;

    public Null(Property prop, boolean not) {
        this.prop = prop;
        this.not = not;
    }

    public Null(String prop, boolean not) {
        this(new Property(prop), not);
    }

    public Property property() {
        return prop;
    }

    public boolean isNegated() {
        return not;
    }

    @Override
    public boolean test(Object obj) {
        return prop.has(obj) && (not != (prop.evaluate(obj) == null));
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.not ? 1 : 0);
        hash = 37 * hash + (this.prop != null ? this.prop.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Null other = (Null) obj;
        if (this.not != other.not) {
            return false;
        }
        if (this.prop != other.prop && (this.prop == null || !this.prop.equals(other.prop))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return prop + " IS" + (not ? " NOT" : "") + " NULL";
    }

}
