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
