package org.jeo.mongo;

import java.io.IOException;

import org.jeo.data.Cursor;
import org.jeo.data.Vector;
import org.jeo.feature.Feature;
import org.jeo.feature.Features;
import org.jeo.feature.Schema;
import org.jeo.geojson.GeoJSON;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class MongoDataset implements Vector {

    DBCollection dbcol;
    Schema schema;

    MongoDataset(DBCollection dbcol) {
        this.dbcol = dbcol;
        this.schema = Features.schema(dbcol.getName(), "loc", Geometry.class);
    }
    
    @Override
    public String getName() {
        return dbcol.getName();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public CoordinateReferenceSystem getCRS() {
        return Proj.EPSG_4326;
    }

    @Override
    public Envelope bounds() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Schema getSchema() throws IOException {
        return schema;
    }

    @Override
    public long count(Envelope bbox) throws IOException {
        if (bbox == null) {
            return dbcol.getCount();
        }


        Polygon p = Geom.toPolygon(bbox);
        DBObject q = BasicDBObjectBuilder.start().push("loc")
            .append("$geoIntersects", JSON.parse(GeoJSON.toString(p))).get();

        return dbcol.getCount(q);
    }

    @Override
    public Cursor<Feature> read(Envelope bbox) throws IOException {
        return null;
    }

    @Override
    public void add(Feature f) throws IOException {
    }

}
