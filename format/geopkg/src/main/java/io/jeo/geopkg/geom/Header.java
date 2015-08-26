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

import com.vividsolutions.jts.geom.Envelope;

class Header {
    Flags flags;
    int srid;
    Envelope envelope;

    public Flags flags() {
        return flags;
    }

    public Header flags(Flags flags) {
        this.flags = flags;
        return this;
    }

    public int srid() {
        return srid;
    }

    public Header srid(int srid) {
        this.srid = srid;
        return this;
    }

    public Envelope envelope() {
        return envelope;
    }

    public Header envelope(Envelope envelope) {
        this.envelope = envelope;
        return this;
    }
}
