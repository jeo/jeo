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
package org.jeo.data;

import java.io.IOException;
import java.util.Map;

/**
 * Interface implemented by Datasets that support transactions. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Transactional {

    /**
     * Creates a new transaction object.
     * 
     * @param options Implementation options to use when creating the transaction, may be 
     * <code>null</code>.
     * 
     * @return A new transaction instance.
     */
    Transaction transaction(Map<String,Object> options) throws IOException;
}
