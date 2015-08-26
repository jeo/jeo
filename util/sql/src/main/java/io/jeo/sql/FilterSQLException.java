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
package io.jeo.sql;

/**
 * Exception thrown when {@link FilterSQLEncoder} is unable to encode a {@link Filter} as SQL. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class FilterSQLException extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    public FilterSQLException(String message) {
        super(message);
    }

    public FilterSQLException(String message, Throwable cause) {
        super(message, cause);
    }
}
