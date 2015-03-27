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

import java.io.Serializable;
import java.util.Locale;

/**
 * A major, minor, patch version number.
 *  
 * @author Justin Deoliveira, Boundless
 */
public class Version implements Serializable, Comparable<Version> {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    Integer major, minor, patch;

    /**
     * Creates a new version object parsing a version string of the format 
     * &lt;major>[.&lt;minor>[.&lt;patch>]].
     * <p>
     * Each component must be parsable as an integer. The minor and patch components are optional.
     * </p>
     * @param version The version string.
     * 
     * @throws IllegalArgumentException If the version string has more than 3 components.
     * @throws NumberFormatException If any of the components is not parsable as an integer.
     */
    public Version(String version) {
        String[] split = version.split("\\.");
        if (split.length > 3) {
            throw new IllegalArgumentException(
                "Unsupported versoin format, must be <major>[.<minor>[.<patch>]]");
        }

        this.major = Integer.parseInt(split[0]);
        this.minor = split.length > 1 ? Integer.parseInt(split[1]) : 0;
        this.patch = split.length > 2 ? Integer.parseInt(split[2]) : 0;
    }

    /**
     * Creates a new version object.
     * 
     * @param major The major number.
     * @param minor The minor number, may be <code>null</code> to signify 0.
     * @param patch The patch number, may be <code>null</code> to signify 0.
     */
    public Version(Integer major, Integer minor, Integer patch) {
        this.major = major != null ? major : 0;
        this.minor = minor != null ? minor : 0;
        this.patch = patch != null ? patch : 0;
    }

    /**
     * The major number component of the version.
     */
    public Integer major() {
        return major;
    }

    /**
     * The minor number component of the version.
     */
    public Integer minor() {
        return minor;
    }

    /**
     * The pach number component of the version.
     */
    public Integer patch() {
        return patch;
    }

    @Override
    public int compareTo(Version v) {
        if (v == null) {
            throw new IllegalArgumentException("Unable to compare to null version");
        }

        int i = major.compareTo(v.major); 
        if (i > 0) {
            return 1;
        }
        else if (i < 0) {
            return -1;
        }
        else {
            int j = minor.compareTo(v.minor);
            if (j > 0) {
                return 1;
            }
            else if (j < 0) {
                return -1;
            }
            else {
                int k = patch.compareTo(v.patch);
                if (k > 0) {
                    return 1;
                }
                else if (k < 0) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((major == null) ? 0 : major.hashCode());
        result = prime * result + ((minor == null) ? 0 : minor.hashCode());
        result = prime * result + ((patch == null) ? 0 : patch.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Version other = (Version) obj;
        if (major == null) {
            if (other.major != null)
                return false;
        } else if (!major.equals(other.major))
            return false;
        if (minor == null) {
            if (other.minor != null)
                return false;
        } else if (!minor.equals(other.minor))
            return false;
        if (patch == null) {
            if (other.patch != null)
                return false;
        } else if (!patch.equals(other.patch))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,"%d.%d.%d", major, minor, patch);
    }
}
