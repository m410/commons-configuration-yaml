package org.m410.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.Test;


import java.io.IOException;
import java.io.StringWriter;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Fortin
 */
public class YamlCombinedConfigurationTest {

    @Test
    public void mergeDependencies() throws ConfigurationException, IOException {
        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());
        final YamlConfiguration first = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/deps1.yml"))
                .getConfiguration();

        final YamlConfiguration second = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/deps2.yml"))
                .getConfiguration();

        combined.addConfiguration(second);
        combined.addConfiguration(first);

        YamlConfiguration configuration = new YamlConfiguration(combined);

        assertEquals(5, configuration.getMaxIndex("dependencies"));

//        StringWriter writer = new StringWriter();
//        configuration.write(writer);
//        writer.close();
//        final String output = writer.toString();
//        System.out.println("---");
//        System.out.println(output);
//        System.out.println("---");
//        assertEquals(6, (output.length() - output.replace("- ", "").length())/2);
    }

    @Test
    public void testComposite() throws ConfigurationException {

        final YamlConfiguration defaultConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(new Parameters().hierarchical().setFileName("src/test/resources/test1.yml"))
                        .getConfiguration();

        final YamlConfiguration overlapConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                        .configure(new Parameters().hierarchical().setFileName("src/test/resources/test2.yml"))
                        .getConfiguration();

        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());
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
