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
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class Maths implements Expression {

    public static final char ADD = '+';
    public static final char SUBTRACT = '-';
    public static final char MULTIPLY = '*';
    public static final char DIVIDE = '/';

    final char operator;
    final Expression left;
    final Expression right;

    public Maths(char operator, Expression left, Expression right) {
        if ("+-*/".indexOf(operator) < 0) {
            throw new IllegalArgumentException(Character.toString(operator));
        }
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public int getOperator() {
        return operator;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public Object evaluate(Object obj) {
        double n1 = ((Number) left.evaluate(obj)).doubleValue();
        double n2 = ((Number) right.evaluate(obj)).doubleValue();
        Double res;
        switch (operator) {
            case ADD: res = n1 + n2; break;
            case SUBTRACT: res = n1 - n2; break;
            case MULTIPLY: res = n1 * n2; break;
            case DIVIDE: res = n1 / n2; break;
            default:
                throw new RuntimeException();
        }
        return res;
    }

    @Override
    public Object accept(FilterVisitor visitor, Object obj) {
        return visitor.visit(this, obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.operator;
        hash = 13 * hash + (this.left != null ? this.left.hashCode() : 0);
        hash = 13 * hash + (this.right != null ? this.right.hashCode() : 0);
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
        final Maths other = (Maths) obj;
        if (this.operator != other.operator) {
            return false;
        }
        if (this.left != other.left && (this.left == null || !this.left.equals(other.left))) {
            return false;
        }
        if (this.right != other.right && (this.right == null || !this.right.equals(other.right))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }

}
