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
package org.jeo.util;

import java.util.ArrayList;
import java.util.List;

/**
 * List of status and error messages.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class Messages {

    /**
     * Obtains a Messages instance.
     * <p>
     * Returns <tt>m</tt> if it is non-null, otherwise it returns a new instance. 
     * </p>
     */
    public static Messages of(Messages m) {
        return Optional.of(m).or(new Messages());
    }

    List<Throwable> messages = new ArrayList<Throwable>();

    /**
     * Reports a new message.
     * 
     * @param msg The message to report.
     */
    public void report(String msg) {
        messages.add(new Throwable(msg));
    }

    /**
     * Reports a new error message.
     *  
     * @param t The error to report. 
     */
    public void report(Throwable t) {
        messages.add(t);
    }

    /**
     * Returns all reported messages. 
     */
    public List<Throwable> list() {
        return messages;
    }
}
