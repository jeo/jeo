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
