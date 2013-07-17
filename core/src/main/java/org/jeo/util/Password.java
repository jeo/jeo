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
