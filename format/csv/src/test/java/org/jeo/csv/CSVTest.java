package org.jeo.csv;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jeo.Tests;
import org.jeo.data.Query;
import org.jeo.feature.Feature;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class CSVTest {

    CSVDataset csv;

    @Before
    public void setUp() throws IOException {
        csv = new CSVDataset(Tests.newTmpFile("jeo", "csv", csv()), new CSVOpts().xy("lon", "lat"));
    }

    @Test
    public void testCount() throws Exception {
        assertEquals(3, csv.count(new Query()));
    }

    @Test
    public void testBounds() throws Exception {
        assertEquals(new Envelope(2, 6, 0, 5), csv.bounds());
    }

    @Test
    public void testCursor() throws Exception {
        for (Feature f : csv.cursor(new Query())) {
            System.out.println(f);
        }
    }

    InputStream csv() {
        StringBuilder sb = new StringBuilder();
        sb.append("name, ").append("cost, ").append("lat, ").append("lon").append("\n");
        sb.append("bomb, ").append("1.99, ").append("0,").append("2").append("\n");
        sb.append("dynamite, ").append("2.99, ").append("3,").append("4").append("\n");
        sb.append("anvil, ").append("3.99,").append("5,").append("6").append("\n");

        return new ByteArrayInputStream(sb.toString().getBytes());
    }
}
