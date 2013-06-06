package org.jeo.map;

/**
 * Defines the rules used to symbolize a map.
 * <p>
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class Style {

    /**
     * Returns a new style builder.
     */
    public static StyleBuilder build() {
        return new StyleBuilder();
    }

    RuleList rules = new RuleList();

    /**
     * The rules making up the style.
     */
    public RuleList getRules() {
        return rules;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Rule r : rules) {
            sb.append(r).append("\n");
        }
        return sb.toString();
    }
}
