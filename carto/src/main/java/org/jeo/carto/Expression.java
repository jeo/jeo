package org.jeo.carto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Expression {

    public static class Simple extends Expression {
        String value;

        public Simple(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "[" + value + "]";
        }
    }

    public static class BinaryExpression extends Expression {
        protected List<Expression> parts;

        BinaryExpression(Expression... e) {
            parts = new ArrayList<Expression>(Arrays.asList(e));
        }

        public List<Expression> getParts() {
            return parts;
        }

        public void append(Expression e) {
            parts.add(e);
        }
    }
    public static class And extends BinaryExpression {
        public And(Expression... e) {
            super(e);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            for (Expression e : parts) {
                b.append(e);
            }
            return b.toString();
        }
    }

    public static class Or extends BinaryExpression {
        public Or(Expression... e) {
            super(e);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            for (Expression e : parts) {
                b.append(e).append(",");
            }
            if (b.length() > 0) {
                b.setLength(b.length()-1);
            }

            return b.toString();
        }
    }
}
