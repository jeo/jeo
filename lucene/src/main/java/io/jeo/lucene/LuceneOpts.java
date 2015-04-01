/* Copyright 2015 The jeo project. All rights reserved.
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
package org.jeo.lucene;

import com.spatial4j.core.context.jts.JtsSpatialContext;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import io.jeo.util.Key;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.jeo.lucene.Lucene.*;

/**
 * Lucene driver options.
 */
public class LuceneOpts {

    public static LuceneOpts fromMap(Map<?,Object> map) {
        LuceneOpts opts = new LuceneOpts(FILE.get(map));
        if (SPATIAL_FIELDS.in(map)) {
            opts.spatialFields(SPATIAL_FIELDS.all(map));
        }
        if (SPATIAL_CONTEXT.in(map)) {
            opts.spatialContext(SPATIAL_CONTEXT.get(map));
        }
        return opts;
    }

    File file;
    Analyzer analyzer = new StandardAnalyzer();
    List<SpatialField> spatialFields = new ArrayList<>();
    JtsSpatialContext spatialContext = SPATIAL_CONTEXT.def();

    public LuceneOpts(File file) {
        Objects.requireNonNull(file);
        this.file = file;
    }

    public File file() {
        return file;
    }

    public Analyzer analyzer() {
        return analyzer;
    }

    public LuceneOpts analyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public LuceneOpts spatialField(String spec) {
        return spatialField(SpatialField.parse(spec, spatialContext));
    }

    public LuceneOpts spatialField(SpatialField field) {
        spatialFields.add(field);
        return this;
    }

    public LuceneOpts spatialFields(List<String> fieldSpecs) {
        for (String spec: fieldSpecs) {
            spatialField(spec);
        }
        return this;
    }

    public List<SpatialField> spatialFields() {
        return spatialFields;
    }

    public JtsSpatialContext spatialContext() {
        return spatialContext;
    }

    public LuceneOpts spatialContext(JtsSpatialContext spatialContext) {
        this.spatialContext = spatialContext;
        return this;
    }

    public Map<Key<?>,Object> toMap() {
        Map<Key<?>,Object> map = new LinkedHashMap<>();
        map.put(Lucene.FILE, file);
        map.put(Lucene.SPATIAL_FIELDS, spatialFields);
        map.put(Lucene.SPATIAL_CONTEXT, spatialContext);
        return map;
    }
}
