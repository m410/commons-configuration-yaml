package org.m410.config;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Michael Fortin
 */
public class YamlCompositeConfigurationTest {

    @Test
    public void testComposite() throws ConfigurationException {
        final XMLBuilderParameters defaultParams = new Parameters().xml().setFileName("src/test/resource/test1.yml");
        final YamlConfiguration defaultConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(defaultParams)
                        .getConfiguration();

        final XMLBuilderParameters overlapParams = new Parameters().xml().setFileName("src/test/resource/test2.yml");
        final YamlConfiguration overlapConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(overlapParams)
                        .getConfiguration();

        CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(overlapConfig);
        configuration.addConfiguration(defaultConfig);

        assertEquals(1222,configuration.getInt("int"));
        assertEquals(false,configuration.getBoolean("boolean"));
        assertEquals("other",configuration.getString("module(org..m410..persistence:jpa).user"));
        assertEquals("some-password",configuration.getString("module(org..m410..persistence:jpa).password"));

        assertEquals("one",configuration.getString("collection-of-map(0).key"));
        assertEquals("two",configuration.getString("collection-of-map(1).key"));
        assertEquals("three",configuration.getString("collection-of-map(2).key"));

        assertEquals(6, configuration.getList("collection").size());
    }
}
