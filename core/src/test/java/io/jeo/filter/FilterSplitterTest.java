package io.jeo.filter;

import io.jeo.util.Pair;
import io.jeo.filter.cql.CQL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterSplitterTest {

    @Test
    public void testFlat() throws Exception {
        Filter f = CQL.parse("foo = TRUE AND x = TRUE");
        Pair<Filter,Filter> split = split(f);
        assertEquals(f, split.first);
        assertEquals(Filters.all(), split.second);

        f = CQL.parse("foo = TRUE AND x = FALSE");
        split = split(f);
        assertEquals(CQL.parse("foo = TRUE"), split.first);
        assertEquals(CQL.parse("x = FALSE"), split.second);

        f = CQL.parse("foo = FALSE AND x = FALSE");
        split = split(f);
        assertEquals(Filters.all(), split.first);
        assertEquals(f, split.second);

        f = CQL.parse("foo = TRUE OR x = TRUE");
        split = split(f);
        assertEquals(f, split.first);
        assertEquals(Filters.all(), split.second);

        f = CQL.parse("foo = TRUE OR x = FALSE");
        split = split(f);
        assertEquals(Filters.all(), split.first);
        assertEquals(f, split.second);

        f = CQL.parse("foo = FALSE OR x = FALSE");
        split = split(f);
        assertEquals(Filters.all(), split.first);
        assertEquals(f, split.second);
    }

    @Test
    public void testNested() throws Exception {
        Filter f = CQL.parse("(foo = TRUE AND x = TRUE) AND z = TRUE");
        Pair<Filter,Filter> split = split(f);
        assertEquals(f, split.first);
        assertEquals(Filters.all(), split.second);

        f = CQL.parse("(foo = TRUE OR x = FALSE) AND z = TRUE");
        split = split(f);
        assertEquals(CQL.parse("z = TRUE"), split.first);
        assertEquals(CQL.parse("foo = TRUE OR x = FALSE"), split.second);

        f = CQL.parse("(foo = TRUE AND x = FALSE) AND z = TRUE");
        split = split(f);
        assertEquals(CQL.parse("foo = TRUE AND z = TRUE"), split.first);
        assertEquals(CQL.parse("x = FALSE"), split.second);

        f = CQL.parse("(foo = TRUE AND x = FALSE) OR z = TRUE");
        split = split(f);
        assertEquals(Filters.all(), split.first);
        assertEquals(CQL.parse("(foo = TRUE AND x = FALSE) OR z = TRUE"), split.second);

    }

    @Test
    public void testNonLogic() throws Exception {
        Filter f = CQL.parse("foo = TRUE");
        Pair<Filter,Filter> split = split(f);
        assertEquals(f, split.first);
        assertEquals(Filters.all(), split.second);

        f = CQL.parse("foo = FALSE");
        split = split(f);
        assertEquals(Filters.all(), split.first);
        assertEquals(f, split.second);
    }


    Pair<Filter,Filter> split(Filter f) {
        FilterSplitter splitter = new FilterSplitter(new FilterWalker<Boolean>() {
            @Override
            public Boolean visit(Comparison<?> compare, Object obj) {
                String s = compare.right().evaluate(null).toString();
                return Boolean.valueOf(s);
            }

            @Override
            public Boolean visit(Logic<?> logic, Object obj) {
                return true;
            }
        });
        return splitter.split(f);
    }
}
