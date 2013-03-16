package org.jeo.agg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jeo.geom.CoordinatePath;
import org.jeo.geom.CoordinatePath.PathStep;

import com.vividsolutions.jts.geom.Coordinate;

public class VertexPathBuffer {

    static final byte STOP = 0;
    static final byte MOVE_TO = 1;
    static final byte LINE_TO = 2;
    static final byte CURVE3 = 3;
    static final byte CURVE4 = 4;
    static final byte CURVEN = 5;
    static final byte CATROM = 6;
    static final byte UBSPLINE = 7;
    static final byte END_POLY = 0x0f;
    static final byte MASK = 0x0f;

    static final int DEFAULT_INITIAL_SIZE = 1024;

    ByteBuffer buf;

    public VertexPathBuffer() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public VertexPathBuffer(int size) {
        buf = ByteBuffer.allocateDirect(size);
        buf.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void reset() {
        buf.clear();
    }

    public ByteBuffer buffer() {
        buf.flip();
        return buf;
    }

    public void fill(CoordinatePath path) {
        while(path.hasNext()) {
            put(path.getStep(), path.next());
        }
        put(PathStep.STOP, null);
    }

    void put(PathStep step, Coordinate c) {
        if (buf.remaining() < 9) {
            expand();
        }

        byte code;

        switch(step) {
        case MOVE_TO:
            code = MOVE_TO; break;
        case LINE_TO:
            code = LINE_TO; break;
        case CLOSE:
            code = END_POLY; break;
        case STOP:
        default:
            code = STOP; break;
        }

        buf.put(code);

        if (code == MOVE_TO || code == LINE_TO) {
            buf.putFloat((float) c.x);
            buf.putFloat((float) c.y);
        }
    }

    void expand() {
        int pos = buf.position();
        buf.rewind();

        ByteBuffer tmp = ByteBuffer.allocateDirect(buf.capacity() * 2);
        tmp.order(buf.order());
        tmp.limit(tmp.capacity());
        tmp.put(buf);
        tmp.position(pos);

        buf = tmp;
    }
}
