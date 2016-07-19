package org.m410.config;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Michael Fortin
 */
public class YamlConfigurationTest {
    YamlConfiguration config;

    @Before
    public void setUp() throws Exception {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<YamlConfiguration> builder =
                new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(params.xml().setFileName("src/test/resource/test1.yml"));

        config = builder.getConfiguration();
    }

    @Test
    public void read() throws Exception {
        assertNotNull(config);

        try{
            config.getBoolean("test-empty");
            fail();
        }
        catch (NoSuchElementException x) {
            assertNotNull(x);
        }

        assertNull(config.getString("test-empty"));
        assertEquals("test value",config.getString("test-string"));
        assertEquals("test value",config.getString("test-place"));
        assertEquals("test value",config.getString("test-interp"));
//        assertEquals("/Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home",config.getString("test-env"));
        assertEquals(12,config.getInt("test-int"));
        assertEquals(Integer.valueOf(12),config.getInteger("test-int",0));
        assertEquals(true,config.getBoolean("test-boolean"));
        assertNotNull(config.getString("test-nested.one"));

        assertEquals("test spaces",config.getString("test string with spaces"));

        Collection<String> collection = config.getCollection(String.class, "test-collection", null);
        assertEquals(3, collection.size());
        assertEquals("one",collection.iterator().next());
        assertEquals("one",config.getList("test-collection").get(0));
    }

    @Test
    public void write() throws Exception {
        final StringWriter file = new StringWriter();
        config.write(file);
        String out = file.toString();
        assertTrue(out.contains("test-int: 12"));
    }
}