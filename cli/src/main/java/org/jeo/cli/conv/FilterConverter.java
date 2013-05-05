package org.jeo.cli.conv;

import org.jeo.filter.Filter;
import org.jeo.filter.cql.CQL;
import org.jeo.filter.cql.ParseException;

import com.beust.jcommander.IStringConverter;

public class FilterConverter implements IStringConverter<Filter> {

    @Override
    public Filter convert(String str) {
        try {
            return CQL.parse(str);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

}
