package org.m410.config;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Michael Fortin
 */
public class YamlCompositeConfigurationTest {

    @Test
    public void testComposite() throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<YamlConfiguration> builder =
                new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(params.xml().setFileName("src/test/resource/test1.yml"));
        final YamlConfiguration defaultConfig = builder.getConfiguration();

        FileBasedConfigurationBuilder<YamlConfiguration> builder2 =
                new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(params.xml().setFileName("src/test/resource/test2.yml"));
        final YamlConfiguration overlapConfig = builder2.getConfiguration();

        CompositeConfiguration configuration = new CompositeConfiguration();
        configuration.addConfiguration(overlapConfig);
        configuration.addConfiguration(defaultConfig);

        assertEquals(1222,configuration.getInt("test-int"));
        assertEquals(false,configuration.getBoolean("test-boolean"));


    }
}
