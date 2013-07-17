package org.jeo.carto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Expr {

    public static class Simple extends Expr {
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

    public static class BinaryExpression extends Expr {
        protected List<Expr> parts;

        BinaryExpression(Expr... e) {
            parts = new ArrayList<Expr>(Arrays.asList(e));
        }

        public List<Expr> getParts() {
            return parts;
        }

        public void append(Expr e) {
            parts.add(e);
        }
    }
    public static class And extends BinaryExpression {
        public And(Expr... e) {
            super(e);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            for (Expr e : parts) {
                b.append(e);
            }
            return b.toString();
        }
    }

    public static class Or extends BinaryExpression {
        public Or(Expr... e) {
            super(e);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            for (Expr e : parts) {
                b.append(e).append(",");
            }
            if (b.length() > 0) {
                b.setLength(b.length()-1);
            }

            return b.toString();
        }
    }
}
