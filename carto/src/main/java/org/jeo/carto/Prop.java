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
package org.jeo.carto;

import org.jeo.filter.Expression;
import org.jeo.filter.Mixed;

public class Prop {

    String key;
    Expression value;

    public Prop(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression expr) {
        if (value != null) {
            if (value instanceof Mixed) {
                value = ((Mixed) value).append(expr);
            }
            else {
                value = new Mixed(value, expr);
            }
        }
        else {
            value = expr;
        }
    }
}
