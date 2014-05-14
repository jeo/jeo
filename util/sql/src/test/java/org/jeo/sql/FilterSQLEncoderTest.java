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
        PrimaryKeyColumn col = new PrimaryKeyColumn("fid", null);
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

        Filter f = Filter.build().property("geom").literal(geo).intersects().filter();
        assertEncoded(f, "ST_Intersects(\"geom\", ST_GeomFromText(?,?))", geo.toText(), 4326);
    }

    void assertEncoded(Filter f, String sql, Object... args) {
        assertEquals(sql, sqle.encode(f, null));
        assertEquals(args.length, sqle.getArgs().size());
        for (int i = 0; i < args.length; i++) {
            assertEquals(args[i], sqle.getArgs().get(i).first());
        }
    }

    @Test
    public void testIn() throws Exception {
        Filter f = Filter.build().property("x").literal("six").literal(6).in().filter();
        assertEquals("\"x\" IN ('six',6)", sqle.encode(f, null));
    }
    
    @Test
    public void testLike() throws Exception {
        Filter f = Filter.build().property("x").literal("foo%bar").like().filter();
        assertEquals("\"x\" LIKE 'foo%bar'", sqle.encode(f, null));
    }

    @Test
    public void testMath() throws Exception {
        Filter f = Filter.build().property("x").literal(42).multiply().literal(2).eq().filter();
        assertEquals("(\"x\"*?) = ?", sqle.encode(f, null));
    }

    @Test
    public void testNull() throws Exception {
        Filter f = Filter.build().property("x").isNull().property("y").isNotNull().or().filter();
        assertEquals("(x IS NULL) OR (y IS NOT NULL)", sqle.encode(f, null));
    }
}
