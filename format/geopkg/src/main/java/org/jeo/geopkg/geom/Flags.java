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

import com.vividsolutions.jts.io.ByteOrderValues;

class Flags {
    byte b;

    Flags(byte b) {
        this.b = b;
    }

    byte getVersion() {
        return (byte) ((b & 0xf0) >> 4);
    }

    void setVersion(byte ver) {
        b |= ((ver << 4) & 0xf0);
    }

    EnvelopeType getEnvelopeIndicator() {
        return EnvelopeType.valueOf((byte) ((b & 0x0e) >> 1));
    }

    void setEnvelopeIndicator(EnvelopeType e) {
        b |= ((e.value << 1) & 0x0e);
    }

    int getEndianess() {
        return (b & 0x01) == 1 ? ByteOrderValues.LITTLE_ENDIAN : ByteOrderValues.BIG_ENDIAN;
    }

    void setEndianess(int endian) {
        byte e = (byte) (endian == ByteOrderValues.LITTLE_ENDIAN ? 1 : 0); 
        b |= (e & 0x01);
    }

    byte toByte() {
        return b;
    }
}
