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

import java.security.SecureRandom;

/**
 * Utility class for handling passwords. 
 * <p>
 * This class stores passwords internally in a character array and not a {@link String}. After the 
 * application is done with the password  it should call {@link #dispose()} in order to scramble
 * the password to help prevent "heap dump" attacks.
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class Password {

    /**
     * Returns the password contents as a string.
     * <p>
     * Note that this method defeats the purpose of this class since it returns a string which will then
     * be stored and pooled in memory. Secure applications should avoid using this method and use {@link #get()}
     * if possible.
     * </p>
     */
    public static String toString(Password passwd) {
        if (passwd == null) {
            return null;
        }
        return new String(passwd.get());
    }

    /**
     * Creates a new password from a string.
     * <p>
     * Note that this method defeats the purpose of this class since it requires a string, which will
     * be pooled in memory. Secure applications should use the constructor of this class if possible.
     * </p>
     */
    public static Password create(String passwd) {
        if (passwd == null) {
            return null;
        }
        return new Password(passwd.toCharArray());
    }

    char[] passwd;

    public Password(char[] passwd) {
        this.passwd = passwd;
    }

    public char[] get() {
        return passwd;
    }

    /**
     * Tests the password against the specified string.
     * 
     * @return True if the text matches this password.
     */
    public boolean matches(String text) {
        return matches(text != null ? text.toCharArray() : null);
    }

    /**
     * Tests the password against the specified character array.
     * 
     * @return True if the text matches this password.
     */
    public boolean matches(char[] text) {
        if (passwd == null) {
            return text == null;
        }

        if (text == null) {
            return false;
        }

        if (passwd.length != text.length) {
            return false;
        }

        for (int i = 0; i < passwd.length; i++) {
            char c = text[i];
            if (passwd[i] != c) {
                return false;
            }
        }

        return true;
    }

    /**
     * Disposes and scrambling the password. 
     */
    public void dispose() {
        if (passwd == null || passwd.length == 0) {
            // nothing to do
            return;
        }

        // scramble the password
        SecureRandom r = new SecureRandom();
        for (int i = 0; i < passwd.length; i++) {
            passwd[i] = (char) r.nextInt(256);
        }
    }
}
