package org.m410.config;

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

    public static YamlConfiguration load(String relativePath) throws ConfigurationException {
        return new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFileName(relativePath))
                .getConfiguration();
    }

    public static YamlConfiguration load(File yamlFile) throws ConfigurationException {
        return new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setFile(yamlFile))
                .getConfiguration();
    }

    public static YamlConfiguration load(URL url) throws ConfigurationException {
        return new FileBasedConfigurationBuilder<>(YamlConfiguration.class)
                .configure(new Parameters().hierarchical().setURL(url))
                .getConfiguration();
    }

    public static YamlConfiguration load(String relativePath, String env) throws ConfigurationException {
        final YamlConfiguration configuration = new YamlConfiguration("environment", env);

        try (FileReader reader = new FileReader(new File(relativePath))) {
            configuration.read(reader);
        }
        catch (IOException e) {
            throw new ConfigurationException(e);
        }

        return configuration;
    }

    public static YamlConfiguration load(File file, String env) throws ConfigurationException {
        final YamlConfiguration configuration = new YamlConfiguration("environment", env);

        try (FileReader reader = new FileReader(file)) {
            configuration.read(reader);
        }
        catch (IOException e) {
            throw new ConfigurationException(e);
        }

        return configuration;
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
