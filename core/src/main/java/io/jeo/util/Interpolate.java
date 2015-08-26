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
package io.jeo.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class containing some simple interpolation functions.
 */
public class Interpolate {

    public static enum Method {
        LINEAR, EXP, LOG; 
    }

    /**
     * Linear interpolation of floating point values.
     * 
     * @param low The min value.
     * @param high The max value.
     * @param n The number of buckets.
     * 
     * @return A list of size <tt>n</tt> + 1.
     */
    public static List<Double> linear(double low, double high, int n) {
        return interpolate(low, high, n, Method.LINEAR);
    }

    /**
     * Exponential interpolation of floating point values.
     * 
     * @param low The min value.
     * @param high The max value.
     * @param n The number of buckets.
     * 
     * @return A list of size <tt>n</tt> + 1.
     */
    public static List<Double> exp(double low, double high, int n) {
        return interpolate(low, high, n, Method.EXP);
    }

    /**
     * Logarithmic interpolation of floating point values.
     * 
     * @param low The min value.
     * @param high The max value.
     * @param n The number of buckets.
     * 
     * @return A list of size <tt>n</tt> + 1.
     */
    public static List<Double> log(double low, double high, int n) {
        return interpolate(low, high, n, Method.LOG);
    }
    
    public static List<Double> interpolate(double low, double high, int n, Method method) {
        return doInterpolate(low, high, n, method);
    }

    static List<Double> doInterpolate(double low, double high, int n, Method method) {
        double delta = high - low;
        Function f = new Function(method , delta);

        List<Double> vals = new ArrayList<Double>(n+1);
        for (int i = 0; i < n+1; i++) {
            vals.add(low + f.calc(i / (double) n));
        }

        return vals;
    }

    static class Function {
        Method method;
        double delta;

        Function(Method method, double delta) {
            this.method = method;
            this.delta = delta;
        }

        public double calc(double v) {
            switch(method) {
            case LINEAR:
                return delta * v;
            case EXP:
                return Math.exp(v * Math.log(1+delta)) - 1;
            case LOG:
                return delta * Math.log((v+1))/Math.log(2);
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
            }
        }
    }
}
