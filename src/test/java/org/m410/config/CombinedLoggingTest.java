package org.m410.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

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

        assertEquals(1, configuration.getMaxIndex("logging(org..m410..garden:garden-logback:0..2-SNAPSHOT).appenders"));
        assertEquals(2, configuration.getMaxIndex("logging(org..m410..garden:garden-logback:0..2-SNAPSHOT).logger"));
        assertEquals("STDOUT", configuration.getString("logging(org..m410..garden:garden-logback:0..2-SNAPSHOT)" +
                                                       ".appenders(0).name"));
        //        assertEquals("FILE",configuration.getString("logging
        // (org..m410..garden:garden-logback:0..2-SNAPSHOT).appenders(1).name"));

        //        final Iterator<String> keys = configuration.getKeys();
        //
        //        while (keys.hasNext()) {
        //            String next = keys.next();
        //            System.out.println(next);
        //        }

        //        final List<ImmutableHierarchicalConfiguration> logger = configuration
        //                .immutableConfigurationsAt("logging(org..m410..garden:garden-logback:0..2-SNAPSHOT)");
        //
        //        for (ImmutableHierarchicalConfiguration log : logger) {
        //            System.out.println(log.getString("appenders(0).name"));
        //        }


        //        try (StringWriter writer = new StringWriter()) {
        //            configuration.write(writer);
        //            System.out.println("");
        //            System.out.println("------");
        //            System.out.println("");
        //            String output = writer.toString();
        //            System.out.println(output);
        //        }
    }
}
