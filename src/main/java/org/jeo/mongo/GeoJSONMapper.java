package org.jeo.mongo;

import static org.jeo.mongo.Functions.BBOX_MAP;
import static org.jeo.mongo.Functions.BBOX_REDUCE;

import org.jeo.feature.Feature;
import org.jeo.geom.Geom;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJSONMapper implements MongoMapper {

    MongoDataset dataset;

    GeoJSONMapper(MongoDataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public Envelope bbox(DBCollection dbcol) {
        MapReduceCommand mr = new MapReduceCommand(
            dbcol, BBOX_MAP, BBOX_REDUCE, null, OutputType.INLINE, new BasicDBObject());

        BasicDBList list = (BasicDBList) dbcol.mapReduce(mr).getCommandResult().get("results");
        DBObject bbox = (DBObject) ((DBObject) list.get(0)).get("value");

        return new Envelope((Double)bbox.get("x1"), (Double)bbox.get("x2"), 
            (Double)bbox.get("y1"), (Double)bbox.get("y2"));
    }

    @Override
    public Feature feature(DBObject obj) {
        return new GeoJSONFeature(obj, dataset.getCollection().getName());
    }

    @Override
    public DBObject object(Feature f) {
        return ((GeoJSONFeature)f).obj;
    }

    @Override
    public DBObject query(Envelope bbox) {
        Polygon p = Geom.toPolygon(bbox);
        return BasicDBObjectBuilder.start().push("geometry").push("$geoIntersects")
            .append("$geometry", GeoJSON.toObject(p)).get();
    }
}
