package org.m410.config;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableHierarchicalConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Fortin
 */
public final class YamlConfig {

    public static List<String> environments(File projectFile) throws ConfigurationException, FileNotFoundException {
        List<String> environments = new ArrayList<>();
        environments.add("default");

        final Iterable<Object> objects = new Yaml().loadAll(new FileReader(projectFile));
        final Iterator<Object> iterator = objects.iterator();

        while (iterator.hasNext()) {
            Map<String, ?> next = (Map<String, ?>) iterator.next();

            if(next.containsKey("environment"))
                environments.add((String)next.get("environment"));
        }

        return environments;
    }

    public static BaseHierarchicalConfiguration load(String relativePath) throws ConfigurationException {
        return new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName(relativePath))
                .getConfiguration();
    }

    public static BaseHierarchicalConfiguration load(File yamlFile) throws ConfigurationException {
        return new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFile(yamlFile))
                .getConfiguration();
    }

    public static BaseHierarchicalConfiguration load(URL url) throws ConfigurationException {
        return new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setURL(url))
                .getConfiguration();
    }

    public static BaseHierarchicalConfiguration load(String relativePath, String env) throws ConfigurationException {
        System.setProperty("fabricate.env",env);
        final YamlEnvConfiguration envConfiguration = new FileBasedConfigurationBuilder<>(YamlEnvConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName(relativePath))
                .getConfiguration();
        System.clearProperty("fabricate.env");

        return envConfiguration;
    }

    public static BaseHierarchicalConfiguration load(File file, String env) throws ConfigurationException {
        System.setProperty("fabricate.env",env);
        final YamlEnvConfiguration envConfiguration = new FileBasedConfigurationBuilder<>(YamlEnvConfiguration.class)
                .configure(new Parameters().hierarchical().setFile(file))
                .getConfiguration();
        System.clearProperty("fabricate.env");

        return envConfiguration;
    }

    public static void write(ImmutableHierarchicalConfiguration c, File file)  {
        try(FileWriter writer = new FileWriter(file)) {
            ((YamlConfiguration)c).write(writer);
        }
        catch (ConfigurationException  | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
