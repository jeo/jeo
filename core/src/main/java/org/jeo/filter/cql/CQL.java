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
package org.jeo.filter.cql;

import org.jeo.filter.Filter;

/**
 * CQL utility class.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class CQL {

    public static Filter parse(String cql) throws ParseException {
        try {
            return new CQLBuilder(cql).parse();
        }
        catch (Throwable orig) {
            try {
                return new ECQLBuilder(cql).parse();
            }
            catch (Throwable e) {
                if (e instanceof ParseException) {
                    throw (ParseException) e;
                }
                if (e instanceof TokenMgrError) {
                    throw (ParseException) new ParseException("Invalid CQL syntax: " + e.getMessage()).initCause(e);
                }
                throw (ParseException) new ParseException("CQL Parsing error").initCause(e);
            }
        }

    }
}
