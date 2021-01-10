package org.m410.config;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ImmutableHierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Michael Fortin
 */
public class FabricateYamlConfigurationTest {
    @Test
    public void mimicFabricateInitialization() throws ConfigurationException {


        CombinedConfiguration combined = new CombinedConfiguration(new UnionCombiner());

        // local-env
        final YamlConfiguration localConfig = YamlConfig.load("src/test/resources/test-local.yml", "development");
        combined.addConfiguration(localConfig,"local");

        // app-env
        final YamlConfiguration envConfig = YamlConfig.load("src/test/resources/test-app.yml", "development");
        combined.addConfiguration(envConfig,"env");

        // app
        final YamlConfiguration overlapConfig = YamlConfig.load("src/test/resources/test-app.yml");
        combined.addConfiguration(overlapConfig,"app");

        // module
        // modules are derived from app configuration

        // default
        final YamlConfiguration defaultConfig = YamlConfig.load("src/test/resources/test-default.yml");
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
    }
}
