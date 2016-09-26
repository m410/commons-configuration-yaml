package org.m410.config;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * @author Michael Fortin
 */
public class EditConfigurationTest {
    @Test
    public void edit() throws IOException, ConfigurationException {
        final YamlConfiguration configuration = new YamlConfiguration();

        try (FileReader reader = new FileReader(new File("src/test/resources/config-example.yml"))) {
            configuration.read(reader);
        }

        assertNotNull(configuration);
        assertEquals("demo2", configuration.getString("application.name"));

        configuration.setProperty("application.name", "garden-updated");

        try (StringWriter writer = new StringWriter()) {
            configuration.write(writer);
            final String out = writer.toString();
            assertTrue(out.contains("garden-updated"));
        }
    }
}
