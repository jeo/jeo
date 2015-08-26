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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Driver registry that uses a static list of driver objects.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class StaticDriverRegistry implements DriverRegistry {

    List<Driver<?>> drivers;

    public StaticDriverRegistry(Driver<?>... drivers) {
        this.drivers = Arrays.asList(drivers);
    }

    @Override
    public Iterator<Driver<?>> list() {
        return drivers.iterator();
    }
}
