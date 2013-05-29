package org.jeo.nano;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Rule;
import org.junit.rules.TestName;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class HandlerTestSupport {

    @Rule
    public TestName name = new TestName();

    Properties q(String s) {
        Properties p = new Properties();
        for (String kvp : s.split("&")) {
            String[] kv = kvp.split("=");
            p.setProperty(kv[0], kv[1]);
        }
        return p;
    }
    
    Properties h(String s) {
        Properties p = new Properties();
        for (String kvp : s.split(";")) {
            String[] kv = kvp.split(": *");
            p.setProperty(kv[0], kv[1]);
        }
        return p;
    }
    
    Properties body(String data) throws IOException {
        File tmp = File.createTempFile("data", name.getMethodName(), new File("target"));
    
        
        ByteStreams.copy(new ByteArrayInputStream(data.getBytes()), 
            Files.newOutputStreamSupplier(tmp));
    
        Properties p = new Properties();
        p.setProperty("content", tmp.getAbsolutePath());
        return p;
    }
}
