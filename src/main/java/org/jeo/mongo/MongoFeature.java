/* Copyright 2013 The jeo project. All rights reserved.
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
package org.jeo.mongo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.jeo.feature.BasicFeature;
import org.jeo.feature.Schema;
import org.jeo.feature.SchemaBuilder;
import org.jeo.geom.Geom;
import org.jeo.proj.Proj;
import org.jeo.util.Util;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Feature wrapper around a mongo {@link DBObject}.
 * <p>
 * This class uses a {@link Mapping} instance to map feature geometries and properties.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class MongoFeature extends BasicFeature {

    MongoFeature(DBObject dbobj, String dbcolName, Mapping mapping) {
        super(id(dbobj), new MongoStorage(dbobj, dbcolName, mapping));
    }

    DBObject object() {
        return ((MongoStorage)storage).obj;
    }

    static String id(DBObject obj) {
        ObjectId id = (ObjectId) obj.get("_id");
        id = id != null ? id : ObjectId.get();
        return id.toString();
    }

    static class MongoStorage extends BasicFeature.Storage {

        DBObject obj;
        String dbcolName;
        Mapping mapping;

        MongoStorage(DBObject dbobj, String dbcolName, Mapping mapping) {
            super(null);
            this.obj = dbobj;
            this.dbcolName = dbcolName;
            this.mapping = mapping;
        }

        @Override
        protected Geometry findGeometry() {
            if (!mapping.getGeometryPaths().isEmpty()) {
                return GeoJSON.toGeometry((DBObject)find(mapping.getGeometryPaths().get(0)));
            }
            return null;
        }

        @Override
        protected Schema buildSchema() {
            SchemaBuilder sb = new SchemaBuilder(dbcolName);

            //add the geometry types
            for (Path g : mapping.getGeometryPaths()) {
                DBObject geo = (DBObject) find(g);
                Geom.Type type = geo != null ? Geom.Type.from((String)geo.get("type")) : Geom.Type.GEOMETRY;
                sb.field(g.join(), type != null ? type.getType() : Geometry.class, Proj.EPSG_4326);
            }

            //add the rest
            DBObject obj = (DBObject) find(mapping.getPropertyPath());
            if (obj != null) {
                for (String key : obj.keySet()) {
                    Object val = obj.get(key);
                    sb.field(key, val != null ? val.getClass() : Object.class);
                }
            }

            return sb.schema();
        }

        @Override
        public Object get(String key) {
            for (Path g : mapping.getGeometryPaths()) {
                if (g.join().equals(key)) {
                    return GeoJSON.toGeometry((DBObject)find(g));
                }
            }
            return find(mapping.getPropertyPath().append(key));
        }

        @Override
        protected Object get(int index) {
            return Util.get(obj.toMap(), index);
        }

        @Override
        public void put(String key, Object val) {
            Object dbval = val;
            if (val instanceof Geometry) {
                dbval = GeoJSON.toObject((Geometry) val);
            }

            for (Path g : mapping.getGeometryPaths()) {
                if (g.join().equals(key)) {
                    if (val instanceof Geometry){
                        set(g, dbval);
                        return;
                    }
                    else {
                        throw new IllegalArgumentException("Value " + val + " is not a geometry");
                    }
                }
            }

            set(mapping.getPropertyPath().append(key), dbval);
        }

        @Override
        protected void set(int index, Object value) {
            if (index >= obj.keySet().size()) {
                throw new IndexOutOfBoundsException(
                    String.format("index: %d, size: %d", index, obj.keySet().size()));
            }

            Iterator<String> it = obj.keySet().iterator();
            for (int i = 0; it.hasNext() && i < index; i++, it.next());

            put(it.next(), value);
        }

        protected void set(Path path, Object val) {
            DBObject obj = this.obj;
            List<String> parts = path.getParts();
            for (int i = 0; i < parts.size()-1; i++) {
                String part = parts.get(i);
                Object next = obj.get(part);
                if (next == null) {
                    next = new BasicDBObject();
                    obj.put(part, next);
                }
        
                if (!(next instanceof DBObject)) {
                    throw new IllegalArgumentException("Illegal path, " + part + " is not an object");
                }
                obj = (DBObject)next; 
            }
        
            obj.put(parts.get(parts.size()-1), val);
        }

        protected Object find(Path path) {
            DBObject obj = this.obj;
            Object next = obj;

            List<String> parts = path.getParts();
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);
                next = obj.get(part);
                if (next == null) {
                    return null;
                }
        
                if (i < parts.size()-1) {
                    if (!(next instanceof DBObject)) {
                        throw new IllegalArgumentException("Illegal path, " + part + " is not an object");
                    }
                    obj = (DBObject) next;
                }
            }
            return next;
        }

        @Override
        public List<Object> list() {
            List<Object> list = new ArrayList<Object>();
        
            for (Path g : mapping.getGeometryPaths()) {
                DBObject geo = (DBObject) find(g);
                list.add(GeoJSON.toGeometry(geo));
            }

            DBObject obj = (DBObject) find(mapping.getPropertyPath()); 
            for (String key : obj.keySet()) {
                list.add(obj.get(key));
            }
            return list;
        }
        
        @Override
        public Map<String, Object> map() {
            Map<String, Object> map = obj.toMap();
            
            for (Path g : mapping.getGeometryPaths()) {
                DBObject geo = (DBObject) find(g);
                map.put(g.join(), GeoJSON.toGeometry(geo));
            }

            return map;
        }
    
    }
}
