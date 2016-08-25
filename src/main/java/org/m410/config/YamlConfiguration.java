package org.m410.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

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

        Map<ImmutableNode, ?> elemRefMap = toNodeMap(yaml.loadAll(in).iterator().next());
        ImmutableNode top = addChildrenToRoot(root, elemRefMap);
        getSubConfigurationParentModel().mergeRoot(top, "yaml", elemRefMap, null, this);
    }

    protected ImmutableNode addChildrenToRoot(ImmutableNode root, Map<ImmutableNode, ?> elemRefMap) {
        ImmutableNode top = root;

        for (ImmutableNode node : elemRefMap.keySet()) {
            if(elemRefMap.get(node) instanceof Map) {
                top = top.addChild(addChildrenToRoot(node, (Map)elemRefMap.get(node)));
            }
            else if(isNodeCollection(elemRefMap.get(node))){
                Collection<Map<ImmutableNode, ?>> n = (Collection<Map<ImmutableNode, ?>>)elemRefMap.get(node);

                for (Map<ImmutableNode, ?> mapValue : n) {
                    top = top.addChild(addChildrenToRoot(node, mapValue));
                }
            }
            else {
                top = top.addChild(node);
            }
        }

        return top;
    }

    protected  boolean isNodeCollection(Object o) {
        // making some assumptions that all elements are the same
        return o instanceof Collection &&
               ((Collection)o).size() > 0 &&
               ((Collection)o).iterator().next() instanceof Map;
    }

    public void write(Writer out) throws ConfigurationException, IOException {
        yaml.dump(fromNodeMap(getModel().getNodeHandler()), out);
    }

    protected Map<ImmutableNode, ?> toNodeMap(Object load) {
        final Map<String,?> map = (Map<String,?>)load;
        final Map<ImmutableNode, Object> nodeMap = new HashMap<>();

        for (String key : map.keySet()) {
            Object value = map.get(key);

            if(value instanceof String || value instanceof Number || value instanceof Boolean) {
                ImmutableNode currentNode = new ImmutableNode.Builder().name(key).value(value).create();
                nodeMap.put(currentNode, value);
            }
            else if(value instanceof Map) {
                ImmutableNode currentNode = new ImmutableNode.Builder().name(key).value(value).create();
                nodeMap.put(currentNode, toNodeMap(value));
            }
            else if (value instanceof Collection){

                if(isNodeCollection(value)) {
                    ImmutableNode currentNode = new ImmutableNode.Builder().name(key).value(value).create();
                    Collection<Map<String, ?>> valueMaps = (Collection<Map<String, ?>>)value;
                    nodeMap.put(currentNode, valueMaps.stream().map(vm -> toNodeMap(vm)).collect(Collectors.toList()));
                }
                else {
                    ImmutableNode currentNode = new ImmutableNode.Builder().name(key).value(value).create();
                    nodeMap.put(currentNode, value);
                }
            }
        }

        return nodeMap;
    }

    protected  Object fromNodeMap(NodeHandler<ImmutableNode> nodeHandler) {
        return toMap(nodeHandler);
    }

    protected  Object toMap(NodeHandler<ImmutableNode> nodeHandler) {
        final ImmutableNode node = nodeHandler.getRootNode();
        Map<String, Object> map = new HashMap<>();

        for (ImmutableNode immutableNode : node.getChildren()) {
            map.put(immutableNode.getNodeName(),immutableNode.getValue());
        }

        return map;
    }
}