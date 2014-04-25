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
package org.jeo.raster;

import org.jeo.util.Dimension;
import org.junit.Test;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DataBufferTest {

    @Test
    public void testGet() throws Exception {
        ByteBuffer bbuf = ByteBuffer.allocate(16);
        bbuf.asIntBuffer().put(1).put(2).put(3).put(4);

        DataBuffer<Byte> nbuf = DataBuffer.create(bbuf, DataType.BYTE);

        for (int i = 0; i < 4; i++) {
            assertEquals(0, nbuf.get().byteValue());
            assertEquals(0, nbuf.get().byteValue());
            assertEquals(0, nbuf.get().byteValue());
            assertEquals((byte)(i+1), nbuf.get().byteValue());
        }
    }

    @Test
    public void testGetAt() throws Exception {
        ByteBuffer bbuf = ByteBuffer.allocate(16);
        bbuf.asIntBuffer().put(1).put(2).put(3).put(4);

        DataBuffer<Byte> nbuf = DataBuffer.create(bbuf, DataType.BYTE);

        for (int i = 0; i < 4; i++) {
            assertEquals((byte) (i + 1), nbuf.get((i + 1) * 4 - 1).byteValue());
        }
    }

    @Test
    public void testPut() throws Exception {
        ByteBuffer bbuf = ByteBuffer.allocate(16);
        DataBuffer<Byte> nbuf = DataBuffer.create(bbuf, DataType.BYTE);
        for (int i = 0; i < 4; i++) {
            nbuf.put((byte)0).put((byte)0).put((byte)0).put((byte) (i + 1));
        }
        bbuf.flip();

        IntBuffer ibuf = bbuf.asIntBuffer();
        assertEquals(1, ibuf.get());
        assertEquals(2, ibuf.get());
        assertEquals(3, ibuf.get());
        assertEquals(4, ibuf.get());
    }

    @Test
    public void testPutAt() throws Exception {
        ByteBuffer bbuf = ByteBuffer.allocate(16);
        for (int i = 0; i < 16; i++) {
            bbuf.put((byte)0);
        }
        DataBuffer<Byte> nbuf = DataBuffer.create(bbuf, DataType.BYTE);
        for (int i = 0; i < 4; i++) {
            nbuf.put((i + 1) * 4 - 1, (byte)(i+1));
        }
        bbuf.flip();

        IntBuffer ibuf = bbuf.asIntBuffer();
        assertEquals(1, ibuf.get());
        assertEquals(2, ibuf.get());
        assertEquals(3, ibuf.get());
        assertEquals(4, ibuf.get());
    }

    @Test
    public void testResample() {
        DataBuffer nbuf = DataBuffer.create(4, DataType.INT);
        nbuf.put(1).put(2).put(3).put(4);

        DataBuffer rbuf = DataBuffer.resample(nbuf, new Dimension(2, 2), new Dimension(4,4));
        assertEquals(1, rbuf.get());
        assertEquals(1, rbuf.get());
        assertEquals(2, rbuf.get());
        assertEquals(2, rbuf.get());
        assertEquals(1, rbuf.get());
        assertEquals(1, rbuf.get());
        assertEquals(2, rbuf.get());
        assertEquals(2, rbuf.get());

        assertEquals(3, rbuf.get());
        assertEquals(3, rbuf.get());
        assertEquals(4, rbuf.get());
        assertEquals(4, rbuf.get());
        assertEquals(3, rbuf.get());
        assertEquals(3, rbuf.get());
        assertEquals(4, rbuf.get());
        assertEquals(4, rbuf.get());

        try {
            rbuf.get();
            fail();
        }
        catch(BufferUnderflowException e) {
        }
    }
}
