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

import java.io.Closeable;
import java.io.IOException;

/**
 * Interface for objects that should be closed after usage.
 * <p>
 * Such objects typically hold onto resources such as file handles, database connections, etc..
 * </p>
 * <p>
 * This interface extends from {@link Closeable} but overrides the {@link #close()} to ensure
 *  no IOException is thrown. 
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public interface Disposable extends Closeable {

    /**
     * Disposes the object. 
     * <p>
     * Unlike {@link Closeable#close()} this method does not throw {@link IOException} and 
     * implementations must ensure no exceptions are thrown from this method.
     * </p>
     * <p>
     * Application code must always be sure to call this method on any implementing class. 
     * Implementing classes should handle multiple calls to this method.  
     * </p>
     */
    void close();
}
