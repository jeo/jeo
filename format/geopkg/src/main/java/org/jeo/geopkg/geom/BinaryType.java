/* Copyright 2015 The jeo project. All rights reserved.
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

public enum BinaryType {

    STANDARD((byte)0),
    EXTENDED((byte)1);

    byte value;

    BinaryType(byte value) {
        this.value = value;
    }

    public byte value() {
        return value;
    }

    public static BinaryType valueOf(byte b) {
        for (BinaryType bt : values()) {
            if (bt.value == b) {
                return bt;
            }
        }
        return null;
    }
}