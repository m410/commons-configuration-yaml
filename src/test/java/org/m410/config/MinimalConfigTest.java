package org.m410.config;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * @author Michael Fortin
 */
public class MinimalConfigTest {
    @Test
    public void shouldHaveOneKey() throws ConfigurationException, IOException {
        final YamlConfiguration first = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/min.yml"))
                .getConfiguration();

        assertNotNull(first);
        assertTrue(first.containsKey("persistence(org..m410..garden:garden-jpa:0..2-SNAPSHOT)"));

        String output = null;

        try (StringWriter writer = new StringWriter()) {
            first.write(writer);
            output = writer.toString();
        }

        assertNotNull(output);
        assertTrue(output.contains("garden"));
    }
}
