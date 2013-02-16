package org.jeo.gmaps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class GMaps {

    public static Envelope bounds(GoogleMap map) {
        LatLngBounds llBounds = map.getProjection().getVisibleRegion().latLngBounds;
        return new GeometryAdapter().adapt(llBounds);
    }

    public static void adapt(Geometry g, GeometryAdapter.Handler h) {
        new GeometryAdapter().adapt(g, h);
    }

    public static LatLngBounds adapt(Envelope e) {
        return new GeometryAdapter().adapt(e);
    }
}
