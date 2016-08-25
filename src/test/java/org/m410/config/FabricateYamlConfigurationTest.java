package org.m410.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Fortin
 */
public class FabricateYamlConfigurationTest {
    @Test
    public void memicFabricateInitialization() throws ConfigurationException {

        System.setProperty("fabricate.env","development");

        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());

        // local-env
        final YamlConfiguration localConfig = new FileBasedConfigurationBuilder<>(YamlEnvConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resource/test-local.yml"))
                .getConfiguration();
        combined.addConfiguration(localConfig,"local");

        // app-env
        final YamlConfiguration envConfig = new FileBasedConfigurationBuilder<>(YamlEnvConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resource/test-app.yml"))
                .getConfiguration();
        combined.addConfiguration(envConfig,"env");


        // app
        final YamlConfiguration overlapConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resource/test-app.yml"))
                .getConfiguration();
        combined.addConfiguration(overlapConfig,"app");

        // module
        // modules are derived from app configuration

        // default
        final YamlConfiguration defaultConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resource/test-default.yml"))
                .getConfiguration();
        combined.addConfiguration(defaultConfig,"default");


        YamlConfiguration configuration = new YamlConfiguration(combined);

        assertNotNull(configuration);
        assertEquals("local-user", configuration.getString("module(org..m410..persistence:jpa:1..0..0).user"));
        System.clearProperty("fabricate.env");
    }
}
