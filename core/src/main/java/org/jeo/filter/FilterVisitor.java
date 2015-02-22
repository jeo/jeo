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

import java.util.List;

/**
 * Visitor for filter classes.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class FilterVisitor {

    public Object visit(Self self, Object obj) {
        return obj;
    }

    public Object visit(Literal literal, Object obj) {
        return obj;
    }

    public Object visit(Property property, Object obj) {
        return obj;
    }

    public Object visit(Function function, Object obj) {
        return obj;
    }

    public Object visit(Mixed mixed, Object obj) {
        for (Expression e : mixed.expressions()) {
            e.accept(this, obj);
        }
        return obj;
    }

    public Object visit(Expression expr, Object obj) {
        return obj;
    }

    public Object visit(All<?> all, Object obj) {
        return obj;
    }

    public Object visit(None<?> none, Object obj) {
        return obj;
    }

    public Object visit(Id<?> id, Object obj) {
        return obj;
    }

    public Object visit(Logic<?> logic, Object obj) {
        for (Filter<?> f : logic.parts()) {
            f.accept(this, obj);
        }
        return obj;
    }

    public Object visit(Comparison<?> compare, Object obj) {
        if (compare.left() != null) {
            compare.left().accept(this, obj);
        }
        if (compare.right() != null) {
            compare.right().accept(this, obj);
        }
        return obj;
    }

    public Object visit(Spatial<?> spatial, Object obj) {
        if (spatial.left() != null) {
            spatial.left().accept(this, obj);
        }
        if (spatial.right() != null) {
            spatial.right().accept(this, obj);
        }
        
        return obj;
    }

    public Object visit(TypeOf<?> inst, Object obj) {
        inst.expression().accept(this, obj);
        return obj;
    }

    public Object visit(In<?> in, Object obj) {
        in.property().accept(this, obj);
        List<Expression> values = in.values();
        for (Expression e: values) {
            e.accept(this, obj);
        }
        return obj;
    }

    public Object visit(Like<?> like, Object obj) {
        like.property().accept(this, obj);
        return obj;
    }

    public Object visit(Math math, Object obj) {
        math.left().accept(this, obj);
        math.right().accept(this, obj);
        return obj;
    }

    public Object visit(Null<?> isNull, Object obj) {
        isNull.property().accept(this, obj);
        return obj;
    }

    public Object visit(Filter<?> filter, Object obj) {
        return obj;
    }
}
