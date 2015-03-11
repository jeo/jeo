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

import java.util.Objects;
import java.util.regex.Pattern;
import org.jeo.vector.Feature;

/**
 * 'Like' predicate filter. Returns true if the String value of the property
 * completely matches the specified match expression. A wild card can be
 * specified using the '%' character. Escaping is not supported and the match
 * is interpreted as a regular expression.
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class Like<T> extends Filter<T> {

    final Property prop;
    final Expression match;
    final Pattern pattern;
    final boolean not;

    public Like(Property prop, Expression match, boolean not) {
        Objects.requireNonNull(prop, "property must not be null");
        Objects.requireNonNull(match, "match must not be null");
        this.prop = prop;
        this.match = match;
        String value = (String) match.evaluate(null);
        // @todo escaping
        this.pattern = Pattern.compile(value.replace("%", ".*"));
        this.not = not;
    }

    public boolean isNegated() {
        return not;
    }

    public Property property() {
        return prop;
    }

    public Expression match() {
        return match;
    }

    public Pattern pattern() {
        return pattern;
    }

    @Override
    public boolean test(T obj) {
        boolean result = false;
        if (obj instanceof Feature) {
            Object val = prop.evaluate((Feature) obj);
            if (val != null) {
                result = pattern.matcher(val.toString()).matches();
            }
        }
        return not != result;
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.prop != null ? this.prop.hashCode() : 0);
        hash = 83 * hash + (this.match != null ? this.match.hashCode() : 0);
        hash = 83 * hash + (this.not ? 1 : 0);
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
        final Like<?> other = (Like<?>) obj;
        if (this.prop != other.prop && (this.prop == null || !this.prop.equals(other.prop))) {
            return false;
        }
        if (this.match != other.match && (this.match == null || !this.match.equals(other.match))) {
            return false;
        }
        if (this.not != other.not) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return prop + (not ? "NOT" : "") + " LIKE " + match;
    }

}
