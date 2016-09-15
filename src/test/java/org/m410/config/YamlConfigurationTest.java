package org.m410.config;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * @author Michael Fortin
 */
public class YamlConfigurationTest {
    YamlConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/test1.yml"))
                .getConfiguration();
    }

    @Test
    public void read() throws Exception {
        assertNotNull(config);

        try {
            config.getBoolean("empty");
            fail();
        }
        catch (NoSuchElementException x) {
            assertNotNull(x);
        }

        assertNull(config.getString("empty"));
        assertEquals("test value", config.getString("string"));
        assertEquals("test value", config.getString("place"));
        assertEquals("test value", config.getString("interp"));
        assertEquals(12, config.getInt("int"));
        assertEquals(Integer.valueOf(12), config.getInteger("int", 0));
        assertEquals(true, config.getBoolean("boolean"));
        assertEquals("test spaces", config.getString("string with spaces"));

        assertEquals("one", config.getString("nested.one"));
        assertEquals("sub1", config.getString("nested.four.four-sub1"));
        assertEquals(3,config.get(Map.class,"nested.four").size());

        assertEquals(3, config.getList("collection").size());
        assertEquals("one on test1", config.getList("collection").get(0));
        assertEquals("two on test1", config.getList("collection").get(1));
        assertEquals("one on test1", config.getString("collection(0)"));
//        assertEquals("two on test1", config.getString("collection(1)"));

        assertEquals("half", config.getString("collection-of-map(0).key"));
        assertEquals(1, config.getInt("collection-of-map(0).version"));

        assertEquals("one", config.getString("collection-of-map(1).key"));
        assertEquals(2, config.getInt("collection-of-map(1).version"));

    }

    @Test
    public void write() throws Exception {
        final StringWriter file = new StringWriter();
        config.write(file);
        String out = file.toString();
//        System.out.println(out);
        assertTrue(out.contains("int: 12"));
    }
}