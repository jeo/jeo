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

import java.nio.ByteBuffer;

/**
 * Wrapper around a {@link java.nio.ByteBuffer} that abstracts away data type of values
 * put into the buffer.
 * <p>
 * The buffer wrapper maintains a native {@link #datatype()}. Values returned from {@link #get()}
 * are of this native type. Values of any type may be put into the buffer via {@link #put(Object)}
 * </p>
 */
public abstract class DataBuffer<T> {
    /**
     * Creates a new wrapper from a byte buffer and specified datatype.
     */
    public static DataBuffer create(ByteBuffer buffer, DataType datatype) {
        switch(datatype) {
            case BYTE:
                return new DataBuffer<Byte>(buffer, datatype) {
                    @Override
                    public Byte get() {
                        return buffer.get();
                    }
                };
            case SHORT:
                return new DataBuffer<Short>(buffer, datatype) {
                    @Override
                    public Short get() {
                        return buffer.getShort();
                    }
                };
            case INT:
                return new DataBuffer<Integer>(buffer, datatype) {
                    @Override
                    public Integer get() {
                        return buffer.getInt();
                    }
                };
            case LONG:
                return new DataBuffer<Long>(buffer, datatype) {
                    @Override
                    public Long get() {
                        return buffer.getLong();
                    }
                };
            case FLOAT:
                return new DataBuffer<Float>(buffer, datatype) {
                    @Override
                    public Float get() {
                        return buffer.getFloat();
                    }
                };
            case DOUBLE:
                return new DataBuffer<Double>(buffer, datatype) {
                    @Override
                    public Double get() {
                        return buffer.getDouble();
                    }
                };
            case CHAR:
                return new DataBuffer<Character>(buffer, datatype) {
                    @Override
                    public Character get() {
                        return buffer.getChar();
                    }
                };
            default:
                throw new IllegalArgumentException("unsupported data type: " + datatype);
        }
    }

    /**
     * Creates a new wrapper by allocating a new buffer of the specified size and type.
     */
    public static DataBuffer create(int size, DataType datatype) {
        switch(datatype) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case CHAR:
                return create(ByteBuffer.allocate(size*datatype.size()), datatype);
            default:
                throw new IllegalArgumentException("unsupported data type: " + datatype);
        }
    }

    /**
     * Resamples the buffer using nearest neighbour interpolation.
     * <p>
     * Resampling works in two dimensions and requires the image dimensions of this buffer and the
     * buffer being resampled to.
     * </p>
     * @param buffer The buffer to resample.
     * @param from The image dimensions of this buffer.
     * @param to The image dimensions of the resampled buffer.
     * @param <T> Buffer type.
     *
     * @return The resampled buffer.
     */
    public static <T> DataBuffer<T> resample(DataBuffer<T> buffer, Dimension from, Dimension to) {
        DataBuffer resampled = create(to.width()*to.height(), buffer.datatype());
        resampled.buffer().order(buffer.buffer().order());

        double xratio = from.width() / (double)to.width();
        double yratio = from.height() / (double)to.height();
        int px, py ;
        for (int j = 0; j < to.height(); j++) {
            int offset = j*to.width();
            for (int i = 0; i < to.width(); i++ ) {
                px = (int) Math.floor(i*xratio);
                py = (int) Math.floor(j*yratio);
                resampled.put(offset+i, buffer.get(py*from.width()+px));
            }
        }
        return resampled.rewind();
    }

    ByteBuffer buffer;
    DataType datatype;
    int word = 0;

    DataBuffer(ByteBuffer buffer, DataType datatype) {
        this.buffer = buffer;
        this.datatype = datatype;
    }

    /**
     * The underlying byte buffer.
     */
    public ByteBuffer buffer() {
        return buffer;
    }

    /**
     * Retrieves the value of type {@link #datatype()} from the current position.
     * <p>
     * This method does no bounds checking to ensure the current position is valid.
     * </p>
     */
    public abstract T get();

    /**
     * Retrieves the value of type {@link #datatype()} at the specified position.
     * <p>
     * The position <tt>i</tt> is relative to the datatype size. For example in a buffer
     * of type {@link DataType#INT} a position of <tt>10</tt> refers to the <tt>10th</tt>
     * integer, not the <tt>10th</tt> byte. In this case it would map to an underlying
     * position of <tt>10*4 = 40</tt>.
     * </p>
     * <p>
     * This method does no bounds checking to ensure the index is valid.
     * </p>
     */
    public T get(int i) {
        buffer.position(i*datatype.size());
        return get();
    }

    /**
     * Puts data into the buffer at the current position.
     * <p>
     * The value <tt>val</tt> must be one of the well known primitive wrapper types
     * (other than Boolean).
     * </p>
     * <p>
     * This method does no bounds checking to ensure the current position is valid.
     * </p>
     */
    public DataBuffer<T> put(Object val) {
        if (val instanceof Byte) {
            buffer.put((Byte)val);
        }
        else if (val instanceof Short) {
            buffer.putShort((Short)val);
        }
        else if (val instanceof Character) {
            buffer.putChar((Character)val);
        }
        else if (val instanceof Integer) {
            buffer.putInt((Integer)val);
        }
        else if (val instanceof Long) {
            buffer.putLong((Long)val);
        }
        else if (val instanceof Float) {
            buffer.putFloat((Float)val);
        }
        else if (val instanceof Double) {
            buffer.putDouble((Double)val);
        }
        else {
            throw new IllegalArgumentException("unknown data value: " + val);
        }
        return this;
    }

    /**
     * Puts data into the buffer at the specified position.
     * <p>
     * The position <tt>i</tt> is relative to the datatype size. For example in a buffer
     * of type {@link DataType#INT} a position of <tt>10</tt> refers to the <tt>10th</tt>
     * integer, not the <tt>10th</tt> byte. In this case it would map to an underlying
     * position of <tt>10*4 = 40</tt>.
     * </p>
     * <p>
     * The value <tt>val</tt> must be one of the well known primitive wrapper types
     * (other than Boolean).
     * </p>
     * <p>
     * This method does no bounds checking to ensure the index is valid.
     * </p>
     */
    public DataBuffer<T> put(int i, T val) {
        buffer.position(i*datatype.size());
        put(val);
        return this;
    }

    /**
     * Flips the buffer.
     *
     * @see {@link java.nio.Buffer#flip()}
     */
    public DataBuffer<T> flip() {
        buffer.flip();
        return this;
    }

    /**
     * Rewinds the buffer.
     * <p>
     * This method resets the word index to 0 in addition to resetting the buffer position.
     * </p>
     *
     * @see {@link java.nio.Buffer#rewind()}
     */
    public DataBuffer<T> rewind() {
        word = 0;
        buffer.rewind();
        return this;
    }

    /**
     * Advances the word index to the next value and sets the buffer position
     * to the same value.
     * <p>
     * This method is used to keep the buffer aligned at "word boundaries" in cases
     * where data types of different sizes are being put into the buffer.
     * </p>
     */
    public DataBuffer<T> word() {
        word += datatype.size();
        buffer.position(word);
        return this;
    }

    /**
     * The size of the buffer, relative to the {@link #datatype()}.
     * <p>
     *  For data types other than {@link DataType#BYTE} this is not the same
     *  as <tt>buffer().capacity()</tt>. Rather it is calculated as:
     *  <pre>
     *      buffer().capacity()/datatype().size()
     *  </pre>
     *
     * </p>
     */
    public int size() {
        return buffer.capacity() / datatype.size();
    }

    /**
     * The data type of the buffer.
     */
    public DataType datatype() {
        return datatype;
    }

}
