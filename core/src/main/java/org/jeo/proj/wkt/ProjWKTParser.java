package org.jeo.proj.wkt;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.datum.Datum;
import org.osgeo.proj4j.datum.Ellipsoid;
import org.osgeo.proj4j.proj.LongLatProjection;
import org.osgeo.proj4j.units.Unit;
import org.osgeo.proj4j.units.Units;

public class ProjWKTParser {

    final static String NAME_KEY = "name";
    final static String IDENTIFIERS_KEY = "identifiers";
    
    public CoordinateReferenceSystem parse(String wkt) throws ParseException {
        return parseCRS(parseTree(wkt));
    }

    Element parseTree(String wkt) throws ParseException {
        return new Element(wkt, new ParsePosition(0));
    }

    CoordinateReferenceSystem parseCRS(Element e) throws ParseException {
        final Object key = e;
        if (key instanceof Element) {
            final String keyword = ((Element) key).keyword.trim().toUpperCase(e.symbols.locale);
            
            try {
                if (   "GEOGCS".equals(keyword)) return parseGeoGCS  (e);
                if (   "PROJCS".equals(keyword)) return parseProjCS  (e);
                if (   "GEOCCS".equals(keyword)) return parseGeoCCS  (e);
                //if (  "VERT_CS".equals(keyword)) return r=parseVertCS  (e);
                //if ( "LOCAL_CS".equals(keyword)) return r=parseLocalCS (e);
                //if ( "COMPD_CS".equals(keyword)) return r=parseCompdCS (e);
                //if ("FITTED_CS".equals(keyword)) return r=parseFittedCS(e);
            } finally {
                // Work around for simulating post-conditions in Java.
                //assert isValid(r, keyword) : element;
            }
        }
        
        throw e.parseFailed(null, String.format("Type \"%s\" is unknown in this context.", key));
    }

    CoordinateReferenceSystem parseGeoGCS(Element element)  throws ParseException {
        //Element            element = parent.pullElement("GEOGCS");
        String                name = element.pullString("name");
        Map<String,?>   properties = parseAuthority(element, name);
        Unit    angularUnit = parseUnit (element, Units.RADIANS);
        
        Object     meridian = parsePrimem   (element, angularUnit);
        Datum datum = parseDatum(element, meridian);

        //String[] params = new String[]{Proj4Keyword.units, angularUnit.abbreviation};
        LongLatProjection proj = new LongLatProjection();
        
        proj.initialize();
        
        return new CoordinateReferenceSystem(name, null, datum, proj);
        /*CoordinateSystemAxis axis0 = parseAxis     (element, angularUnit, false);
        CoordinateSystemAxis axis1;
        CoordinateSystemAxis axis2 = null;
        try {
            if (axis0 != null) {
                axis1 = parseAxis(element, angularUnit, true);
                if(axis1 != null) {
                    axis2 = parseAxis(element, SI.METER, false);
                } 
            } else {
                // Those default values are part of WKT specification.
                axis0 = createAxis(null, "Lon", AxisDirection.EAST,  angularUnit);
                axis1 = createAxis(null, "Lat", AxisDirection.NORTH, angularUnit);
            }
            element.close();
            EllipsoidalCS ellipsoidalCS;
            if(axis2 != null) {
                ellipsoidalCS = csFactory.createEllipsoidalCS(properties, axis0, axis1, axis2);
            } else {
                ellipsoidalCS = csFactory.createEllipsoidalCS(properties, axis0, axis1);
            }
            return crsFactory.createGeographicCRS(properties, datum,
                    ellipsoidalCS);
        } catch (FactoryException exception) {
            throw element.parseFailed(exception, null);
        }*/
    }

    Map<String,Object> parseAuthority(final Element parent, final String name)
        throws ParseException {

        final boolean isRoot = parent.isRoot();
        final Element element = parent.pullOptionalElement("AUTHORITY");
        Map<String,Object> properties;
        if (element == null) {
            if (isRoot) {
                properties = new HashMap<String,Object>(4);
                properties.put(NAME_KEY, name);
            } else {
                properties = Collections.singletonMap(NAME_KEY, (Object) name);
            }
        } else {
            final String auth = element.pullString("name");
            // the code can be annotation marked but could be a number to
            String code = element.pullOptionalString("code");
            if (code == null) {
                int codeNumber = element.pullInteger("code");
                code = String.valueOf(codeNumber);
            }
            element.close();
            
            //final Citation authority = Citations.fromName(auth);
            properties = new HashMap<String,Object>(4);
            properties.put(NAME_KEY, auth + ":" + name);
            properties.put(IDENTIFIERS_KEY, auth + ":" + code);
        }
        if (isRoot) {
            //properties = alterProperties(properties);
        }
        return properties;
    }

    Unit parseUnit(final Element parent, final Unit unit) throws ParseException {
        final Element element = parent.pullElement("UNIT");
        final String     name = element.pullString("name");
        final double   factor = element.pullDouble("factor");
        
        final Map<String,?> properties = parseAuthority(element, name);
        element.close();

        
        if (name != null) {
            Unit u = Units.findUnits(name.toLowerCase());
            if (u != null) {
                return u;
            }
        }

        return (factor != 1) ? times(unit, factor) : unit;
    }

    Unit times(Unit u, double factor) {
        throw new UnsupportedOperationException();
    }

    Object parsePrimem(final Element parent, final Unit angularUnit)
            throws ParseException
    {
        final Element   element = parent.pullElement("PRIMEM");
        final String       name = element.pullString("name");
        final double  longitude = element.pullDouble("longitude");
        final Map<String,?> properties = parseAuthority(element, name);
        element.close();

        return null;
    }

    Datum parseDatum(final Element parent, final Object meridian) throws ParseException {
        Element element = parent.pullElement("DATUM");
        String name = element.pullString("name");
        Ellipsoid ellipsoid = parseSpheroid(element);
        
        double[] toWGS84 = parseToWGS84(element); // Optional; may be
                                                             // null.
        Map<String, Object> properties = parseAuthority(element, name);
        if (true/*ALLOW_ORACLE_SYNTAX*/ && (toWGS84 == null)
                && (element.peek() instanceof Number)) {
            toWGS84 = new double[7];
            toWGS84[0] = element.pullDouble("dx");
            toWGS84[1] = element.pullDouble("dy");
            toWGS84[2] = element.pullDouble("dz");
            toWGS84[3] = element.pullDouble("ex");
            toWGS84[4] = element.pullDouble("ey");
            toWGS84[5] = element.pullDouble("ez");
            toWGS84[6] = element.pullDouble("ppm");
        }
        element.close();

        return new Datum(name, toWGS84, ellipsoid, name);
    }

    Ellipsoid parseSpheroid(final Element parent) throws ParseException {
        Element          element = parent.pullElement("SPHEROID");
        String              name = element.pullString("name");
        double     semiMajorAxis = element.pullDouble("semiMajorAxis");
        double inverseFlattening = element.pullDouble("inverseFlattening");
        Map<String,?> properties = parseAuthority(element, name);
        element.close();
        if (inverseFlattening == 0) {
            // Inverse flattening null is an OGC convention for a sphere.
            inverseFlattening = Double.POSITIVE_INFINITY;
        }

        return new Ellipsoid(name, semiMajorAxis, 0, inverseFlattening, name);
    }

    double[] parseToWGS84(final Element parent) throws ParseException {
        final Element element = parent.pullOptionalElement("TOWGS84");
        if (element == null) {
            return null;
        }

        double dx  = element.pullDouble("dx");
        double dy  = element.pullDouble("dy");
        double dz  = element.pullDouble("dz");

        try {
            if (element.peek() != null) {
                double ex  = element.pullDouble("ex");
                double ey  = element.pullDouble("ey");
                double ez  = element.pullDouble("ez");
                double ppm = element.pullDouble("ppm");
                return new double[]{dx, dy, dz, ex, ey, ez, ppm};
            }
            else {
                return new double[]{dx, dy, dz};
            }
        }
        finally {
            element.close();    
        }

    }
    
    CoordinateReferenceSystem parseProjCS(Element e) throws ParseException {
        // TODO Auto-generated method stub
        return null;
    }

    CoordinateReferenceSystem parseGeoCCS(Element e)  throws ParseException {
        // TODO Auto-generated method stub
        return null;
    }
}
