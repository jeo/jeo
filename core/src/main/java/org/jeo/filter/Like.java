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

import java.util.regex.Pattern;
import org.jeo.feature.Feature;

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
    final Pattern match;
    final boolean not;

    public Like(Property prop, Expression match, boolean not) {
        this.prop = prop;
        String value = (String) match.evaluate(null);
        // @todo escaping
        this.match = Pattern.compile(value.replace("%", ".*"));
        this.not = not;
    }

    public boolean isNegated() {
        return not;
    }

    public Property getProperty() {
        return prop;
    }

    public Pattern getMatch() {
        return match;
    }

    @Override
    public boolean apply(T obj) {
        boolean result = false;
        if (obj instanceof Feature) {
            Object val = prop.evaluate((Feature) obj);
            if (val != null) {
                result = match.matcher(val.toString()).matches();
            }
        }
        return not != result;
    }

}
