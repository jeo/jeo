package org.jeo.agg;

import org.jeo.geom.CoordinatePath;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class VertexSource {

    /**
     * Enum for path command, mirrors the path_commands_e enum from agg_basics.h.
     */
    public static enum PathCommand {

        STOP(0), MOVE_TO(1), LINE_TO(2), CURVE3(3), CURVE4(4), CURVEN(5), CATROM(6), UBSPLINE(7),
        END_POLY(0x0F), MASK(0x0F);
        
        byte code;

        PathCommand(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }
    }

    CoordinatePath it;

    public VertexSource(Geometry g) {
        it = CoordinatePath.create(g);
    }

    /**
     * Rewinds the specified path.
     * 
     * @pathId The path id, 0 for default.
     */
    public void rewind(int pathId) {
        if (pathId == 0) {
            it.reset();
        }
    }

    /**
     * Sets the value of the current vertex.
     * 
     * The command specifying what to do at the next vertex.
     */
    public byte vertex(double[] xy) {
        if (!it.hasNext()) {
            return PathCommand.STOP.getCode();
        }

        Coordinate c = it.next();
        xy[0] = c.x;
        xy[1] = c.y;

        switch(it.getStep()) {
        case MOVE_TO:
            return PathCommand.MOVE_TO.getCode();
        case LINE_TO:
            return PathCommand.LINE_TO.getCode();
        case CLOSE:
            return PathCommand.LINE_TO.getCode();
        case STOP:
            return PathCommand.STOP.getCode();
        default:
            return PathCommand.STOP.getCode();
        }
    }
}
