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
import java.util.List;

/**
 * Expression that evaluates to the result of a function.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class Function implements Expression {

    protected String name;

    protected List<Expression> args;

    protected Function(String name) {
        this.name = name;
        this.args = new ArrayList<Expression>();
    }

    /**
     * The name of the function.
     */
    public String getName() {
        return name;
    }

    /**
     * Function arguments.
     */
    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public Object accept(FilterVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }
}
