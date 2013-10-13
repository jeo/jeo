package org.jeo.csv;

import java.io.File;

import org.jeo.Tests;
import org.jeo.data.VectorApiTestBase;
import org.jeo.data.VectorDataset;

public class CSVApiTest extends VectorApiTestBase {

    @Override
    protected VectorDataset createVectorData() throws Exception {
        File tmp = Tests.newTmpDir("states", "csv");
        Tests.unzip(getClass().getResourceAsStream("states.csv.zip"), tmp);

        return CSV.open(new File(tmp, "states.csv"), new CSVOpts().wkt("wkt")
            .delimiter(Delimiter.character(';')));
    }
}
