/* Copyright 2014 The jeo project. All rights reserved.
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
package io.jeo.raster;

/**
 * Statistics for a raster band including mean, standard deviation, and extrema (min/max).
 */
public class Stats {
    double min, max, stdev, mean;

    public Stats() {
        this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public Stats(double min, double max, double stdev, double mean) {
        this.min = min;
        this.max = max;
        this.stdev = stdev;
        this.mean = mean;
    }

    /**
     * The minimum value of the band.
     */
    public double min() {
        return min;
    }

    /**
     * Sets the minimum value of the band.
     */
    public Stats min(double min) {
        this.min = min;
        return this;
    }

    /**
     * The maximum value of the band.
     */
    public double max() {
        return max;
    }

    /**
     * Sets the maximum value of the band.
     */
    public Stats max(double max) {
        this.max = max;
        return this;
    }

    /**
     * The standard deviation of the band.
     */
    public double stdev() {
        return stdev;
    }

    /**
     * Sets the standard deviation of the band.
     */
    public Stats stdev(double stdev) {
        this.stdev = stdev;
        return this;
    }

    /**
     * The average value of the band.
     */
    public double mean() {
        return mean;
    }

    /**
     * Sets the average value of the band.
     */
    public Stats mean(double mean) {
        this.mean = mean;
        return this;
    }
}
