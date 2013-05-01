package org.jeo.proj.wkt;

import java.text.ParseException;

import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.datum.Datum;
import org.osgeo.proj4j.datum.Ellipsoid;
import org.osgeo.proj4j.proj.LongLatProjection;
import org.osgeo.proj4j.proj.Projection;
import org.osgeo.proj4j.units.Unit;
import org.osgeo.proj4j.units.Units;

public class ProjWKTEncoder {

    private static String q = "\"";

    public String encode(CoordinateReferenceSystem crs) throws ParseException {
        return encode(crs, true);
    }
    
    public String encode(CoordinateReferenceSystem crs, boolean format) throws ParseException {
        return encodeCRS(crs, new StringBuilder(), format ? 0 : -1);
    }

    String encodeCRS(CoordinateReferenceSystem crs, StringBuilder buf, int indent) {
        return crs.getProjection() instanceof LongLatProjection ? 
                encodeGeoCRS(crs, buf, indent) : encodeProjCS(crs, buf, indent);
    }

    String encodeGeoCRS(CoordinateReferenceSystem crs, StringBuilder buf, int indent) {
        doIndent(buf, indent).append("GEOGCS[").append(q).append(crs.getName()).append(q).append(",");
        encodeDatum(crs.getDatum(), buf, addIndent(indent)).append(",");

        encodePrimeMeridian(crs, buf, addIndent(indent)).append(",");
        encodeUnit(crs.getProjection(), Units.RADIANS, buf, addIndent(indent)).append(",");
        encodeAxis("Geodetic longitude", "EAST", buf, addIndent(indent)).append(",");
        encodeAxis("Geodetic latitude", "NORTH", buf, addIndent(indent));
        buf.append("]");
        return buf.toString();
    }

    StringBuilder encodeDatum(Datum datum, StringBuilder buf, int indent) {
        doIndent(buf, indent).append("DATUM[").append(q).append(datum.getName()).append(q).append(",");
        encodeEllipsoid(datum.getEllipsoid(), buf, addIndent(indent));

        double[] toWgs84 = datum.getTransformToWGS84();
        if (toWgs84 != null) {
            buf.append(",");
            encodeTOWGS84(toWgs84, buf, addIndent(indent));
        }
        return buf.append("]");

    }

    StringBuilder encodeEllipsoid(Ellipsoid e, StringBuilder buf, int indent) {
        double invFlat = e.getA() / (e.getA() - e.getB());
        return doIndent(buf, indent).append("SPHEROID[")
            .append(q).append(e.getName()).append(q).append(", ")
            .append(e.getEquatorRadius()).append(", ").append(invFlat)
            .append("]");
        
    }

    StringBuilder encodeTOWGS84(double[] toWgs84, StringBuilder buf, int indent) {
        doIndent(buf, indent).append("TOWGS84[");
        for (double d : toWgs84) {
            buf.append(d).append(", ");
        }
        buf.setLength(buf.length()-2);
        return buf.append("]");
    }

    StringBuilder encodePrimeMeridian(CoordinateReferenceSystem crs, StringBuilder buf, int indent) {
        doIndent(buf, indent).append("PRIMEM[").append(q).append("Greenwich").append(q)
            .append(", 0.0]");
        return buf;
    }

    StringBuilder encodeUnit(Projection p, Unit base, StringBuilder buf, int indent) {
        Unit unit = p.getUnits();
        double scale = Units.convert(1, unit, base);
        doIndent(buf, indent).append("UNIT[").append(q).append(unit.name).append(q)
            .append(", ").append(scale).append("]");
        return buf;
    }
    
    StringBuilder encodeAxis(String name, String dir, StringBuilder buf, int indent) {
        doIndent(buf, indent).append("AXIS[").append(q).append(name).append(q)
        .append(", ").append(dir).append("]");
        return buf;
    }

    String encodeProjCS(CoordinateReferenceSystem crs, StringBuilder buf, int indent) {
        doIndent(buf, indent).append("PROJCS[").append(q).append(crs.getName()).append(q).append(",");
        encodeGeoCRS(crs.createGeographic(), buf, addIndent(indent));
        buf.append(",");

        Projection p = crs.getProjection();
        encodeProjection(p, buf, addIndent(indent)).append(",");
        encodeParameter("central_meridian", p.getProjectionLongitudeDegrees(), buf, addIndent(indent)).append(",");
        encodeParameter("latitude_of_origin", p.getProjectionLatitudeDegrees(), buf, addIndent(indent)).append(",");

        encodeParameter("scale_factor", p.getScaleFactor(), buf, addIndent(indent)).append(",");
        encodeParameter("false_easting", p.getFalseEasting(), buf, addIndent(indent)).append(",");
        encodeParameter("false_northing", p.getFalseNorthing(), buf, addIndent(indent)).append(",");
        
        encodeUnit(crs.getProjection(), Units.METRES, buf, addIndent(indent)).append(",");
        encodeAxis("Easting", "EAST", buf, addIndent(indent)).append(",");
        encodeAxis("Northing", "NORTH", buf, addIndent(indent));
        buf.append("]");
        return buf.toString();
    }

    StringBuilder encodeProjection(Projection p, StringBuilder buf, int indent) {
        String name = p.getWktName();
        if (name == null) {
            name = p.getName();
        }
        doIndent(buf, indent).append("PROJECTION[").append(q).append(name).append(q).append("]");
        return buf;
    }

    StringBuilder encodeParameter(String key, double val, StringBuilder buf, int indent) {
        return doIndent(buf, indent).append("PARAMETER[")
            .append(q).append(key).append(q).append(", ")
            .append(val).append("]");
    }

    StringBuilder doIndent(StringBuilder buf, int indent) {
        if (indent > -1) {
            if (indent > 0) {
                buf.append("\n");
            }
            for (int i = 0; i < indent; i++) {
                buf.append(" ");
            }
        }
        return buf;
    }

    int addIndent(int indent) {
        return indent > -1 ? indent + 2 : indent;
    }
}
