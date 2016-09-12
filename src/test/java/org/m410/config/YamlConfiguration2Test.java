package org.m410.config;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Fortin
 */
public class YamlConfiguration2Test {
    @Test
    public void readlarge() throws ConfigurationException {
        final YamlConfiguration configuration = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/config-example.yml"))
                .getConfiguration();
        assertNotNull(configuration);
    }

}
