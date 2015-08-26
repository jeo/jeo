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
package io.jeo.geojson.parser;

import java.io.IOException;

import io.jeo.json.parser.ParseException;
import org.osgeo.proj4j.CoordinateReferenceSystem;

public class CRSFinder extends BaseHandler {

    CoordinateReferenceSystem crs;

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    @Override
    public void startJSON() throws ParseException, IOException {
    }
    
    @Override
    public void endJSON() throws ParseException, IOException {
    }
    
    @Override
    public boolean startObject() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean endObject() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean startObjectEntry(String key) throws ParseException, IOException {
        if ("crs".equals(key)) {
            push(key, new CRSHandler() {
               @Override
                public boolean endObject() throws ParseException, IOException {
                   super.endObject();
                   crs = (CoordinateReferenceSystem) node.getValue();
                   return false;
                } 
            });
        }
        return true;
    }
    
    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean startArray() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean endArray() throws ParseException, IOException {
        return true;
    }
    
    @Override
    public boolean primitive(Object value) throws ParseException, IOException {
        return true;
    }

}
