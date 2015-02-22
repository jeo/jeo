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

/**
 * An expression composed of other expressions.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class Mixed implements Expression {

    List<Expression> exprs = new ArrayList<Expression>();

    public Mixed(Expression... exprs) {
        this(Arrays.asList(exprs));
    }

    public Mixed(List<Expression> exprs) {
        this.exprs = new ArrayList<Expression>(exprs);
    }

    public List<Expression> expressions() {
        return exprs;
    }

    @Override
    public Object evaluate(Object obj) {
        List<Object> result = new ArrayList<Object>();
        for (Expression expr: exprs) {
            result.add(expr.evaluate(obj));
        }
        return result;
    }

    @Override
    public Object accept(FilterVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }

    /**
     * Appends an expression to the mix.
     */
    public Mixed append(Expression expr) {
        Mixed mixed = new Mixed(exprs);
        mixed.exprs.add(expr);
        return mixed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression e : exprs){
            sb.append(" ").append(e.toString());
        }
        return sb.toString();
    }
}
