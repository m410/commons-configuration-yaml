package org.m410.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.NodeModel;
import org.yaml.snakeyaml.Yaml;

public class YamlConfiguration extends BaseHierarchicalConfiguration implements FileBasedConfiguration {

    private final Yaml yaml = new Yaml();

    public YamlConfiguration() {
        super();
    }

    public YamlConfiguration(HierarchicalConfiguration<ImmutableNode> c) {
        super(c);
    }

    protected YamlConfiguration(NodeModel<ImmutableNode> model) {
        super(model);
    }


    public void read(Reader in) throws ConfigurationException, IOException {
        ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        ImmutableNode top = rootBuilder.create();
        Map<ImmutableNode, Object> elemRefMap = toNodeMap(yaml.load(in), top);
        getSubConfigurationParentModel().mergeRoot(top, "yaml", elemRefMap, null, this);
    }

    public void write(Writer out) throws ConfigurationException, IOException {
        yaml.dump(fromNodeMap(getModel().getNodeHandler()), out);
    }

    private Map<ImmutableNode, Object> toNodeMap(Object load, ImmutableNode parent) {
        final Map<String,Object> map = (Map<String,Object>)load;
        final Map<ImmutableNode, Object> nodeMap = new HashMap<>();

        for (String key : map.keySet()) {
            Object value = map.get(key);
            ImmutableNode immutableNode = new ImmutableNode.Builder().name(key).create();
            parent.addChild(immutableNode);

            if(value instanceof String || value instanceof Number || value instanceof Boolean) {
                nodeMap.put(immutableNode, value);
            }
            else if(value instanceof Map) {
                toNodeMap(value, immutableNode);
            }
            else if (value instanceof Collection){
                nodeMap.put(immutableNode, value);
            }
        }

        return nodeMap;
    }

    private Object fromNodeMap(NodeHandler<ImmutableNode> nodeHandler) {
        return null;
    }
}