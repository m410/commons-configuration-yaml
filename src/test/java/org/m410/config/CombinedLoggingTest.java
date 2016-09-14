package org.m410.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Michael Fortin
 */
public class CombinedLoggingTest {
    @Test
    public void mergeLogging() throws ConfigurationException, IOException {
        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());
        final YamlConfiguration first = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/log1.yml"))
                .getConfiguration();
        combined.addConfiguration(first);

        final YamlConfiguration second = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/log2.yml"))
                .getConfiguration();
        combined.addConfiguration(second);

        YamlConfiguration configuration = new YamlConfiguration(combined);
        String output = null;

        try (StringWriter writer = new StringWriter()) {
            configuration.write(writer);
            output = writer.toString();
            System.out.println(output);
        }
    }
}
