package org.m410.config;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.ConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Michael Fortin
 */
public class YamlConfigurationTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void read() throws Exception {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<YamlConfiguration> builder =
                new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(params.xml().setFileName("src/test/resource/test1.yml"));

        YamlConfiguration config = builder.getConfiguration();
        assertNotNull(config);

        System.out.println(config.getRootElementName());
        final Iterator<String> keys = config.getKeys();

        while (keys.hasNext()) {
            System.out.println(keys.next());
        }

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
        assertEquals("/Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home",config.getString("test-env"));
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

    }


}