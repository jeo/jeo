package org.jeo.svg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jeo.TestData;
import org.jeo.Tests;
import org.jeo.data.Query;
import org.jeo.filter.Property;
import org.jeo.geom.Envelopes;
import org.jeo.map.Map;
import org.jeo.map.RGB;
import org.jeo.map.Style;
import org.jeo.map.View;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.vividsolutions.jts.geom.Envelope;

import static org.jeo.map.CartoCSS.*;

public class SVGRendererTest {

    @Rule
    public TestName testName = new TestName();

    ByteArrayOutputStream output;
    SVGRenderer r;

    @Before
    public void setUp() throws IOException {
       output = new ByteArrayOutputStream();
       r = new SVGRenderer().indent(2);
    }

    @Test
    public void background() throws Exception {
        Style s = Style.build().select("Map").set(BACKGROUND_COLOR, "red").style();

        r.init(Map.build().size(512, 512).style(s).view(), null);
        r.render(output);
    }

    @Test
    public void points() throws Exception {
        Style s = Style.build()
            .select("Map").set("debug-labels", "false")
            .select("*")
                .set(MARKER_FILL, "green")
                .set(MARKER_WIDTH, 10)
                .set(TEXT_NAME, new Property("name"))
                .set(TEXT_ALIGN, "middle")
                .set(TEXT_FACE_NAME, "Arial")
                .set(TEXT_DY, 5)
                .style();

        Envelope bbox = Envelopes.scale(TestData.point().bounds(), 1.5);
        View v = Map.build().layer(TestData.point()).style(s).bounds(bbox).size(256,256).view();
        r.init(v, null);
        r.render(output);
    }

    @Test
    public void lines() throws IOException {
        Style s = Style.build().select("*")
            .set(LINE_DASHARRAY, "5 3 2 5 3 2")
            .set(LINE_WIDTH, 2)
            .style();

        View v = Map.build().layer(TestData.line()).style(s).view();
        r.init(v, null);
        r.render(output);
    }

    @Test
    public void polygons() throws IOException {
        Style s = Style.build().select("*")
           .set(POLYGON_FILL, RGB.gray)
           .style();

        View v = Map.build().layer(TestData.states()).style(s).view();
        r.init(v, null);
        r.render(output);
    }

    //uncomment to write out resulting svg to home directory
    //@After
    public void debugOutput() throws IOException {
        File file = new File("target", testName.getMethodName() + ".svg");
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(output.toByteArray()), file); 
    }
}
