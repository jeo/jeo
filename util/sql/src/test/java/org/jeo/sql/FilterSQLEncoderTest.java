package org.jeo.sql;

import static org.junit.Assert.*;

import org.jeo.filter.Filter;
import org.jeo.geom.GeomBuilder;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

public class FilterSQLEncoderTest {

    FilterSQLEncoder sqle;

    @Before
    public void setUp() {
        PrimaryKeyColumn col = new PrimaryKeyColumn("fid");
        PrimaryKey pkey = new PrimaryKey();
        pkey.getColumns().add(col);

        sqle = new FilterSQLEncoder();
        sqle.setPrimaryKey(pkey);
        sqle.setDbTypes(new DbTypes());
    }

    @Test
    public void testCompare() throws Exception {
        Filter f = Filter.build().property("foo").literal("bar").eq().filter();
        assertEncoded(f, "\"foo\" = ?", "bar");

        f = Filter.build().property("foo").literal(12).lt().filter();
        assertEncoded(f, "\"foo\" < ?", 12);

        f = Filter.build().property("foo").literal(14.0).lte().filter();
        assertEncoded(f, "\"foo\" <= ?", 14.0);

        f = Filter.build().literal(12).property("foo").gt().filter();
        assertEncoded(f, "? > \"foo\"", 12);

        f = Filter.build().literal("bar").property("foo").gte().filter();
        assertEncoded(f, "? >= \"foo\"", "bar");

        f = Filter.build().literal("twelve").literal(12).neq().filter();
        assertEncoded(f, "? != ?", "twelve", 12);
    }

    @Test
    public void testLogic() throws Exception {
        Filter f = Filter.build().property("foo").literal("bar").eq()
            .property("oof").literal("rab").eq().and().filter();
        assertEncoded(f, "(\"foo\" = ?) AND (\"oof\" = ?)", "bar", "rab");
    }

    @Test
    public void testSpatial() throws Exception {
        Geometry geo = new GeomBuilder(4326).point(1, 2).toPoint();

        Filter f = Filter.build().property("geom").literal(geo).intersect().filter();
        assertEncoded(f, "ST_Intersects(\"geom\", ST_GeomFromText(?,?))", geo.toText(), 4326);
    }

    void assertEncoded(Filter f, String sql, Object... args) {
        assertEquals(sql, sqle.encode(f, null));
        assertEquals(args.length, sqle.getArgs().size());
        for (int i = 0; i < args.length; i++) {
            assertEquals(args[i], sqle.getArgs().get(i).first());
        }
    }
}
