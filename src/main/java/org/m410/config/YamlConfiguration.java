package org.m410.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
        ImmutableNode root = rootBuilder.create();
        Map<ImmutableNode, ?> elemRefMap = toNodeMap(yaml.load(in), root);
        ImmutableNode top = addChildrenToRoot(root, elemRefMap);
        getSubConfigurationParentModel().mergeRoot(top, "yaml", elemRefMap, null, this);
    }

    private ImmutableNode addChildrenToRoot(ImmutableNode root, Map<ImmutableNode, ?> elemRefMap) {
        ImmutableNode top = root;

        for (ImmutableNode immutableNode : elemRefMap.keySet()) {
            if(elemRefMap.get(immutableNode) instanceof Map) {
                top = top.addChild(addChildrenToRoot(immutableNode, (Map)elemRefMap.get(immutableNode)));
            }
            else {
                top = top.addChild(immutableNode);
            }
        }

        return top;
    }

    public void write(Writer out) throws ConfigurationException, IOException {
        yaml.dump(fromNodeMap(getModel().getNodeHandler()), out);
    }

    private Map<ImmutableNode, Object> toNodeMap(Object load, ImmutableNode parent) {
        final Map<String,?> map = (Map<String,?>)load;
        final Map<ImmutableNode, Object> nodeMap = new HashMap<>();

        for (String key : map.keySet()) {
            Object value = map.get(key);
            ImmutableNode currentNode = new ImmutableNode.Builder().name(key).value(value).create();

            if(value instanceof String || value instanceof Number || value instanceof Boolean) {
                nodeMap.put(currentNode, value);
            }
            else if(value instanceof Map) {
                nodeMap.put(currentNode, toNodeMap(value, currentNode));
            }
            else if (value instanceof Collection){
                nodeMap.put(currentNode, value);
            }
        }

        return nodeMap;
    }

    private Object fromNodeMap(NodeHandler<ImmutableNode> nodeHandler) {
        return toMap(nodeHandler);
    }

    private Object toMap(NodeHandler<ImmutableNode> nodeHandler) {
        final ImmutableNode node = nodeHandler.getRootNode();
        Map<String, Object> map = new HashMap<>();

        for (ImmutableNode immutableNode : node.getChildren()) {
            map.put(immutableNode.getNodeName(),immutableNode.getValue());
        }

        return map;
    }
}