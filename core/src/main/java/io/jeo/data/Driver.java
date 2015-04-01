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
package io.jeo.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.jeo.util.Key;
import io.jeo.util.Messages;

/**
 * Format driver.
 * <p>
 * The job of a driver is to create a "connection" to data in a particular format. A driver can 
 * return either one of a dataset, or a workspace containing many datasets. 
 * </p>
 * <p>
 * Driver implementations must adhere to the following guidelines:
 * <ul>
 *  <li>Be thread safe and ideally maintain no state</li>
 *  <li>Contain a single no argument constructor</li>
 * </ul>
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface Driver<T> {

    /**
     * Name identifying the driver.
     * <p>
     * This name should be no more than a few words (ideally one). It isn't meant to be a 
     * description but should be human readable. 
     * </p>
     */
    String name();

    /**
     * Secondary names identifying the driver.
     * <p>
     * Aliases are typically shorter abbreviations for {@link #name()}
     * </p>
     */
    List<String> aliases();

    /**
     * Returns the class of object returned by the driver.
     */
    Class<T> type();

    /**
     * Returns the keys supported by the driver.
     */
    List<Key<?>> keys();

    /**
     * Determines if the driver is enabled.
     * <p>
     * The <tt>messages</tt> argument is optionally used for the driver to report back any messages
     * or exceptions that prevent the driver from being enabled.
     * </p>
     */
    boolean isEnabled(Messages messages);

    /**
     * Determines if this driver can open a connection to the data described by the specified
     * options.
     * <p>
     * The <tt>messages</tt> list is optionally used for the driver to report back any messages
     * or exceptions that prevent the driver from opening the specified data source.
     * </p>
     * @param opts Options describing the data.
     * @param messages Messages reported from the driver, optionally <code>null</code>.
     * 
     * @return True if the driver can open the data, otherwise false.
     */
    boolean canOpen(Map<?,Object> opts, Messages messages);

    /**
     * Opens a connection to data described by the specified options.
     * 
     * @param opts Options describing the data to connect to.
     * 
     * @return The data.
     * 
     * @throws IOException In the event of a connection error such as a file system error or 
     *   database connection failure. 
     */
    T open(Map<?,Object> opts) throws IOException;
}
