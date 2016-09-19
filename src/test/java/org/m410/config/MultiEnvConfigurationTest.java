package org.m410.config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Michael Fortin
 */
public class MultiEnvConfigurationTest {

    @Test
    public void loadDevelopment() throws IOException, ConfigurationException {
        File file = new File("src/test/resources/multiconfig.yml");
        final YamlConfiguration configuration = new YamlConfiguration("environment", "development");

        try (FileReader reader = new FileReader(file)) {
            configuration.read(reader);
        }

        assertNotNull(configuration);
        assertEquals("this is development", configuration.getString("common_value"));
    }

    @Test
    public void loadDevelopmentByIndex() throws IOException, ConfigurationException {
        File file = new File("src/test/resources/multiconfig.yml");
        final YamlConfiguration configuration = new YamlConfiguration(1);

        try (FileReader reader = new FileReader(file)) {
            configuration.read(reader);
        }

        assertNotNull(configuration);
        assertEquals("this is development", configuration.getString("common_value"));
    }

    @Test
    public void loadDefault() throws IOException, ConfigurationException {
        File file = new File("src/test/resources/multiconfig.yml");
        final YamlConfiguration configuration = new YamlConfiguration();

        try (FileReader reader = new FileReader(file)) {
            configuration.read(reader);
        }

        assertNotNull(configuration);
        assertEquals("this is default", configuration.getString("common_value"));
    }

    @Test
    public void loadDefaultNotfound() throws IOException, ConfigurationException {
        File file = new File("src/test/resources/multiconfig.yml");
        final YamlConfiguration configuration = new YamlConfiguration("environment", "default");

        try (FileReader reader = new FileReader(file)) {
            configuration.read(reader);
        }

        assertNotNull(configuration);
        assertNull(configuration.getString("common_value"));
    }

}
