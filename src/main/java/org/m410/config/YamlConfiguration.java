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
import org.apache.commons.configuration2.tree.InMemoryNodeModel;
import org.apache.commons.configuration2.tree.NodeHandler;
import org.apache.commons.configuration2.tree.NodeModel;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlConfiguration extends BaseHierarchicalConfiguration implements FileBasedConfiguration {

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
        Yaml yaml = new Yaml();
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
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        final InMemoryNodeModel nodeModel = this.getNodeModel();
        yaml.dump(fromNodeMap(getModel().getInMemoryRepresentation()), out);
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

    protected  Map<String, Object> fromNodeMap(ImmutableNode node) {
        return toMap(new HashMap<>(), node);
    }

    protected  Map<String, Object> toMap(Map<String, Object> map, ImmutableNode node) {

        for (ImmutableNode immutableNode : node.getChildren()) {
            final Object value = immutableNode.getValue();
            final String name = immutableNode.getNodeName();

            if(map.containsKey(name)) {
                if(value instanceof Map) {
                    ((Map<String,Object>)map.get(name)).putAll((Map)value);
                }
                else if (value instanceof Collection){

                    if(isNodeCollection(value)) {
                        Collection<Map<String, Object>> valueMaps = (Collection<Map<String, Object>>)value;
                        List<Map<String, Object>> collect = valueMaps.stream()
                                .map(vm -> toMap(vm, immutableNode))
                                .collect(Collectors.toList());
                        ((Collection<Map<String, Object>>)map.get(name)).addAll(collect);
                    }
                    else {
                        ((Collection)map.get(name)).addAll((Collection)value);
                    }
                }
            }
            else {
                if(value instanceof String || value instanceof Number || value instanceof Boolean) {
                    map.put(name,immutableNode.getValue());
                }
                else if(value instanceof Map) {
                    map.put(name,value);
                }
                else if (value instanceof Collection){

                    if(isNodeCollection(value)) {
                        Collection<Map<String, Object>> valueMaps = (Collection<Map<String, Object>>)value;
                        List<Map<String, Object>> collect = valueMaps.stream()
                                .map(vm -> toMap(vm, immutableNode))
                                .collect(Collectors.toList());
                        map.put(name, collect);
                    }
                    else {
                        map.put(name,value);
                    }
                }
            }
        }

        return map;
    }
}