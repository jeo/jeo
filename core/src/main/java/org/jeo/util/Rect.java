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
package org.jeo.util;

import java.util.Locale;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Rectangle class.
 * <p>
 * Rectangle coordinates use an "image" coordinate system in which the top left corner
 * is considered (0,0) and the bottom right corner considered (width, height).
 * </p>
 */
public class Rect {

    public final int left, bottom, right, top;

    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Rect(int left, int top, Dimension size) {
        this.left = left;
        this.top = top;
        this.right = left + size.width();
        this.bottom = top + size.height();
    }

    /**
     * Width of rectangle.
     */
    public int width() {
        return right - left;
    }

    /**
     * Height of rectangle.
     */
    public int height() {
        return bottom - top;
    }

    /**
     * Dimensions of rectangle.
     */
    public Dimension size() {
        return new Dimension(width(), height());
    }

    /**
     * Area of rectangle.
     * <p>
     * For rectangles representing raster images this is the number of pixels
     * in the image.
     * </p>
     */
    public int area() {
        return width() * height();
    }

    /**
     * Scales the rectangle.
     *
     * @param scx The horizontal scaling factor.
     * @param scy The vertical scaling factor.
     *
     * @return The new scaled rectangle.
     */
    public Rect scale(double scx, double scy) {
        return new Rect(left, top, (int)(left+width()*scx), (int)(top+height()*scy));
    }

    /**
     * Determines if this rectangle intersects another rectangle.
     *
     * @param other The other rectangle.
     *
     * @return True if the two rectangles intersect otherwise false.
     */
    public boolean intersects(Rect other) {
        return !(other.left > right ||
                other.right < left ||
                other.top > bottom ||
                other.bottom < top);
    }

    /**
     * Intersects two rectangles.
     *
     * @param other The other rectangle.
     * @return The rectangle intersection, otherwise <code>null</code> if the two don't
     *   intersect.
     */
    public Rect intersect(Rect other) {
        if (!intersects(other)) {
            return null;
        }

        int l = left > other.left ? left : other.left;
        int t = top > other.top ? top : other.top;
        int r = right < other.right ? right : other.right;
        int b = bottom < other.bottom ? bottom : other.bottom;

        return new Rect(l, t, r, b);
    }

    /**
     * Converts the rectangle to an Envelope.
     */
    public Envelope envelope() {
        return new Envelope(left, right, top, bottom);
    }

    /**
     * Creates a new rectangle based on the relative positions of two envelopes.
     *
     * @param bbox The first envelope meant to correspond to this rectangle.
     * @param other The envelope corresponding to the return envelope.
     *
     * @return The rectangle corresponding to other.
     */
    public Rect map(Envelope bbox, Envelope other) {
        int l = (int)((bbox.getMinX() - other.getMinX())/other.getWidth() * width());
        int t = (int)((other.getMaxY() - bbox.getMaxY())/other.getHeight() * height());
        int w = (int)(bbox.getWidth() / other.getWidth() * width());
        int h = (int)(bbox.getHeight() / other.getHeight() * height());

        return new Rect(l, t, l+w, t+h);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rect rect = (Rect) o;

        if (bottom != rect.bottom) return false;
        if (left != rect.left) return false;
        if (right != rect.right) return false;
        if (top != rect.top) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + bottom;
        result = 31 * result + right;
        result = 31 * result + top;
        return result;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,"Rect(%d,%d,%d,%d)", left, top, right, bottom);
    }
}
