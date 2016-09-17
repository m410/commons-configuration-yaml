package org.m410.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Fortin
 */
public class YamlCombinedConfigurationTest {

    @Test
    public void writeMerged() throws ConfigurationException, IOException {
        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());
        final YamlConfiguration first = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/deps1.yml"))
                .getConfiguration();
        combined.addConfiguration(first);

        final YamlConfiguration second = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/deps2.yml"))
                .getConfiguration();
        combined.addConfiguration(second);

        final YamlConfiguration third = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/deps3.yml"))
                .getConfiguration();
        combined.addConfiguration(third);

        final YamlConfiguration fourth = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/deps4.yml"))
                .getConfiguration();
        combined.addConfiguration(fourth);

        YamlConfiguration configuration = new YamlConfiguration(combined);
        String output = null;

        try (StringWriter writer = new StringWriter()) {
            configuration.write(writer);
            output = writer.toString();
            //            System.out.println(output);
        }

        assertEquals(16, configuration.getMaxIndex("dependencies"));
        //        assertEquals("commons-lang", configuration.getString("dependencies(0).name"));
        //        assertEquals("commons-util", configuration.getString("dependencies(1).name"));
        //        assertEquals("h2", configuration.getString("dependencies(2).name"));
        //        assertEquals("hibernate-entitymanager", configuration.getString("dependencies(3).name"));
        //        assertEquals("hibernate-validator", configuration.getString("dependencies(4).name"));
        //        assertEquals("slf4j-api", configuration.getString("dependencies(5).name"));


        assertEquals(17, (output.length() - output.replace("- ", "").length()) / 2);
    }

    @Test
    public void testComposite() throws ConfigurationException, IOException {

        final YamlConfiguration defaultConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/test1.yml"))
                .getConfiguration();

        final YamlConfiguration overlapConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/test2.yml"))
                .getConfiguration();

        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());
        combined.addConfiguration(overlapConfig, "app");
        combined.addConfiguration(defaultConfig, "default");
        YamlConfiguration configuration = new YamlConfiguration(combined);

        assertEquals(1222, configuration.getInt("int"));
        assertEquals(false, configuration.getBoolean("boolean"));
        assertEquals("other", configuration.getString("module(org..m410..persistence:jpa).user"));
        assertEquals("some-password", configuration.getString("module(org..m410..persistence:jpa).password"));

        assertEquals("one", configuration.getString("collection-of-map(0).key"));
        assertEquals("two", configuration.getString("collection-of-map(1).key"));
        assertEquals("three", configuration.getString("collection-of-map(2).key"));
        assertEquals("four", configuration.getString("collection-of-map(3).key"));
        assertEquals("half", configuration.getString("collection-of-map(4).key"));

        assertEquals("four on test2", configuration.getString("collection(0)"));
        assertEquals("three", configuration.getString("nested.one"));
        assertEquals("four", configuration.getString("nested.two"));
        assertEquals("five", configuration.getString("nested.three"));
        assertEquals("sub-three", configuration.getString("nested.four.four-sub1"));

        StringWriter writer = new StringWriter();
        configuration.write(writer);
        writer.close();
        final String output = writer.toString();
        //        System.out.println(output);
        assertEquals(11, (output.length() - output.replace("- ", "").length()) / 2);
    }
}
