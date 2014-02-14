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
 * Identity filter that always returns true.
 */
public class All<T> extends Filter<T> {

    @Override
    public boolean apply(T obj) {
        return true;
    }

    @Override
    public Object accept(FilterVisitor v, Object obj) {
        return v.visit(this, obj);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof All;
    }

    @Override
    public int hashCode() {
        return All.class.getName().hashCode();
    }

    @Override
    public String toString() {
        return "All";
    }
}
