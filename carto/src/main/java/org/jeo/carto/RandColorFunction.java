package org.jeo.carto;

import java.util.Random;

import org.jeo.filter.Function;
import org.jeo.map.RGB;

/**
 * Function that evaluates to a random color.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class RandColorFunction extends Function {

    Random r;
    
    public RandColorFunction() {
        super("randcolor");
        r = new Random();
    }
    
    @Override
    public Object evaluate(Object obj) {
        return new RGB(r.nextInt(255), r.nextInt(255), r.nextInt(255));
    }
}
