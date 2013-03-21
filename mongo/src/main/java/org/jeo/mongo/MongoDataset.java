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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class MongoDataset implements Vector {

    DBCollection dbcol;
    MongoMapper mapper;
    Schema schema;

    MongoDataset(DBCollection dbcol) {
        this.dbcol = dbcol;
        this.schema = Features.schema(dbcol.getName(), "geometry", Geometry.class);
        this.mapper = new GeoJSONMapper();
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
        return bbox != null ? dbcol.count(query(bbox)) : dbcol.count();
    }

    @Override
    public Cursor<Feature> read(Envelope bbox) throws IOException {
        DBCursor dbCursor = bbox != null ? dbcol.find(query(bbox)) : dbcol.find();
        return new MongoCursor(dbCursor, mapper);
    }

    DBObject query(Envelope bbox) {
        Polygon p = Geom.toPolygon(bbox);
        return BasicDBObjectBuilder.start().push("geometry").push("$geoIntersects")
            .append("$geometry", JSON.parse(GeoJSON.toString(p))).get();
    }

    @Override
    public void add(Feature f) throws IOException {
        BasicDBObject obj = new BasicDBObject();
        mapper.feature(obj, f);
        dbcol.insert(obj);
    }

}
