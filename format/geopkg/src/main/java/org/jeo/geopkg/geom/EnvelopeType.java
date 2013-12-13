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
package org.jeo.geopkg.geom;

public enum EnvelopeType {
    NONE(0, 0), XY(1, 32), XYZ(2, 48), XYM(3, 48), XYZM(4, 64);

    byte value;
    byte length;

    EnvelopeType(int value, int length) {
        this.value = (byte) value;
        this.length = (byte) length;
    }

    public static EnvelopeType valueOf(byte b) {
        for (EnvelopeType et : values()) {
            if (et.value == b) return et;
        }
        return null;
    }
}
