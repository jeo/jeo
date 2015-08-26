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
 * Numeric data type enumeration.
 */
public enum DataType {

    CHAR {
        @Override
        protected int bits() {
            return Character.SIZE;
        }
    },
    BYTE {
        @Override
        protected int bits() {
            return Byte.SIZE;
        }
    },
    SHORT {
        @Override
        protected int bits() {
            return Short.SIZE;
        }
    },
    INT {
        @Override
        protected int bits() {
            return Integer.SIZE;
        }
    },
    LONG {
        @Override
        protected int bits() {
            return Long.SIZE;
        }
    },
    FLOAT {
        @Override
        protected int bits() {
            return Float.SIZE;
        }
    },
    DOUBLE {
        @Override
        protected int bits() {
            return Double.SIZE;
        }
    };

    /**
     * The size of the datatype in bytes.
     */
    public int size() {
        return bits() / 8;
    }

    /**
     * The size of the datatype in bits.
     */
    protected abstract int bits();
}

