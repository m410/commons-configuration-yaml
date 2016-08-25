package org.m410.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Michael Fortin
 */
public class YamlCombinedConfigurationTest {

    @Test
    public void testComposite() throws ConfigurationException {

        final YamlConfiguration defaultConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(new Parameters().hierarchical().setFileName("src/test/resource/test1.yml"))
                        .getConfiguration();

        final YamlConfiguration overlapConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(new Parameters().hierarchical().setFileName("src/test/resource/test2.yml"))
                        .getConfiguration();

        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());
        // local
        // env
        combined.addConfiguration(overlapConfig,"app");
        combined.addConfiguration(defaultConfig,"default");
        YamlConfiguration configuration = new YamlConfiguration(combined);

        assertEquals(1222,configuration.getInt("int"));
        assertEquals(false,configuration.getBoolean("boolean"));
        assertEquals("other",configuration.getString("module(org..m410..persistence:jpa).user"));
        assertEquals("some-password",configuration.getString("module(org..m410..persistence:jpa).password"));

        assertEquals("one",configuration.getString("collection-of-map(0).key"));
        assertEquals("two",configuration.getString("collection-of-map(1).key"));
        assertEquals("three",configuration.getString("collection-of-map(2).key"));
        assertEquals("four",configuration.getString("collection-of-map(3).key"));
        assertEquals("half",configuration.getString("collection-of-map(4).key"));

        assertEquals("four on test2", configuration.getString("collection(0)"));
        assertEquals("three",configuration.getString("nested.one"));
        assertEquals("four",configuration.getString("nested.two"));
        assertEquals("five",configuration.getString("nested.three"));
        assertEquals("sub-three",configuration.getString("nested.four.four-sub1"));

    }
}
