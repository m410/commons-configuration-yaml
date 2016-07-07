package org.m410.config;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

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
        FileBasedConfigurationBuilder<YamlConfiguration> builder = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(params.xml().setFileName("src/test/resource/test1.yml"));
        YamlConfiguration config = builder.getConfiguration();
        assertNotNull(config);
        System.out.println(config.getRootElementName());
        final Iterator<String> keys = config.getKeys();

        while (keys.hasNext()) {
            System.out.println(config.getKeys().next());
        }

        assertEquals("test",config.getString("yaml.test"));
    }

    @Test
    public void write() throws Exception {

    }

}