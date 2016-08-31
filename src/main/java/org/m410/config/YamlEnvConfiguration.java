package org.m410.config;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.NodeModel;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlEnvConfiguration extends YamlConfiguration {

    private final Yaml yaml = new Yaml();

    public YamlEnvConfiguration() {
        super();
    }

    public YamlEnvConfiguration(HierarchicalConfiguration<ImmutableNode> c) {
        super(c);
    }

    protected YamlEnvConfiguration(NodeModel<ImmutableNode> model) {
        super(model);
    }

    @Override
    public void read(Reader in) throws ConfigurationException, IOException {
        ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        ImmutableNode root = rootBuilder.create();

        String env = System.getProperty("fabricate.env");
        final Iterable<Object> objects = yaml.loadAll(in);
        Map<String, ?> foundConfig = null;

        for (Object object : objects) {
            Map<String, ?> configs = (Map<String, ?>) object;

            if(env.equalsIgnoreCase("default") && configs.get("environment") == null) {
                foundConfig =  configs;
            }
            else if (configs.get("environment") != null && configs.get("environment").equals(env)) {
                foundConfig =  configs;
            }
        }

        if(foundConfig != null) {
            Map<ImmutableNode, ?> elemRefMap = toNodeMap(foundConfig);
            ImmutableNode top = addChildrenToRoot(root, elemRefMap);
            getSubConfigurationParentModel().mergeRoot(top, "yaml", elemRefMap, null, this);
        }
        else { // not found, but have to initialize something
            Map<ImmutableNode, ?> elemRefMap = toNodeMap(new HashMap<>());
            ImmutableNode top = addChildrenToRoot(root, elemRefMap);
            getSubConfigurationParentModel().mergeRoot(top, "yaml", elemRefMap, null, this);
        }
    }
}