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
package io.jeo.geopkg.geom;

import com.vividsolutions.jts.io.ByteOrderValues;

class Flags {

    static final byte ENDIAN_MASK = (byte) 0x01;
    static final byte ENVELOPE_MASK = (byte) 0x0e;
    private static byte EMPTY_MASK = (byte) 0x10;
    static final byte BINARY_MASK = (byte) 0x20;

    byte b;

    Flags(byte b) {
        this.b = b;
    }

    int endianess() {
        return (b & ENDIAN_MASK) == 1 ? ByteOrderValues.LITTLE_ENDIAN : ByteOrderValues.BIG_ENDIAN;
    }

    Flags endianess(int endian) {
        byte e = (byte) (endian == ByteOrderValues.LITTLE_ENDIAN ? 1 : 0); 
        b |= (e & ENDIAN_MASK);
        return this;
    }

    EnvelopeType envelopeIndicator() {
        return EnvelopeType.valueOf((byte) ((b & ENVELOPE_MASK) >> 1));
    }

    Flags envelopeIndicator(EnvelopeType e) {
        b |= ((e.value << 1) & ENVELOPE_MASK);
        return this;
    }

    boolean empty() {
        return (b | EMPTY_MASK) == 1;
    }

    Flags empty(boolean empty) {
        b |= ((byte) (empty ? 1 : 0) & EMPTY_MASK);
        return this;
    }

    BinaryType binaryType() {
        return BinaryType.valueOf((byte) ((b & BINARY_MASK) >> 1));
    }

    Flags binaryType(BinaryType bt) {
        b |= ((bt.value << 1) & BINARY_MASK);
        return this;
    }

    byte toByte() {
        return b;
    }
}
