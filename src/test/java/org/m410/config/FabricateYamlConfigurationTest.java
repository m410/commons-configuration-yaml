package org.m410.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ImmutableHierarchicalConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Fortin
 */
public class FabricateYamlConfigurationTest {
    @Test
    public void mimicFabricateInitialization() throws ConfigurationException {

        System.setProperty("fabricate.env","development");

        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());

        // local-env
        final YamlConfiguration localConfig = new FileBasedConfigurationBuilder<>(YamlEnvConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/test-local.yml"))
                .getConfiguration();
        combined.addConfiguration(localConfig,"local");

        // app-env
        final YamlConfiguration envConfig = new FileBasedConfigurationBuilder<>(YamlEnvConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/test-app.yml"))
                .getConfiguration();
        combined.addConfiguration(envConfig,"env");

        // app
        final YamlConfiguration overlapConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/test-app.yml"))
                .getConfiguration();
        combined.addConfiguration(overlapConfig,"app");

        // module
        // modules are derived from app configuration

        // default
        final YamlConfiguration defaultConfig = new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName("src/test/resources/test-default.yml"))
                .getConfiguration();
        combined.addConfiguration(defaultConfig,"default");


        ImmutableHierarchicalConfiguration configuration = new YamlConfiguration(combined);

        assertNotNull(configuration);
        assertEquals("local-user", configuration.getString("module(org..m410..persistence:jpa:1..0..0).user"));
        assertEquals("development-password", configuration.getString("module(org..m410..persistence:jpa:1..0..0).password"));
        assertEquals("jdbc:sqlserver://default-host:123/database?name=value", configuration.getString("module(org..m410..persistence:jpa:1..0..0).url"));
        assertTrue(!configuration.getBoolean("module(org..m410..persistence:jpa:1..0..0).properties.reconnect"));
        assertEquals("select current_timestamp",configuration.getString("module(org..m410..persistence:jpa:1..0..0).properties.query"));
        assertEquals(1,configuration.getInt("module(org..m410..persistence:jpa:1..0..0).properties.size"));
        assertEquals(10,configuration.getInt("module(org..m410..persistence:jpa:1..0..0).properties.max"));

        System.clearProperty("fabricate.env");
    }
}
