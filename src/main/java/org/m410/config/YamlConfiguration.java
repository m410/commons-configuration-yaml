package org.m410.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.*;
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

    protected  static boolean isNodeCollection(Object o) {
        // making some assumptions that all elements are the same
        return o instanceof Collection &&
               ((Collection)o).size() > 0 &&
               ((Collection)o).iterator().next() instanceof Map;
    }

    public void write(Writer out) throws ConfigurationException, IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        yaml.dump(fromNode(), out);
    }

    private Map<String, Object> fromNode() {
        final YamlBuilder visitor = new YamlBuilder();
        NodeTreeWalker.INSTANCE.walkBFS(getNodeModel().getRootNode(), visitor,getNodeModel().getNodeHandler());
        return visitor.getDocument();
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


    static final class YamlBuilder extends BuilderVisitor {
        Map<String, Object> document = new HashMap<>();
        List<Shadow> documentShadow = new ArrayList<>();

        protected YamlBuilder() {
            super();
            documentShadow.add(new ShadowMapNode("yaml","yaml",null));
        }

        @Override
        protected void insert(final ImmutableNode node, final ImmutableNode parent,
                final ImmutableNode siblingBefore, final ImmutableNode siblingAfter,
                final ReferenceNodeHandler handler) {

            final String nodeName = node.getNodeName();
            final String longNodeName = toLongName(handler.getParent(node), handler, node.getNodeName().replaceAll("\\.", "\\^"));
            final Object value = node.getValue();

            Shadow shadow = by(longNodeName).orElseGet(() -> makeShadow(parent, handler, nodeName, longNodeName, value));

            shadow.syncDocument(value);
        }

        YamlConfiguration.YamlBuilder.Shadow makeShadow(ImmutableNode parent, ReferenceNodeHandler handler,
                String nodeName, String longNodeName, Object value) {
            YamlConfiguration.YamlBuilder.Shadow shadowParent = by(longNodeName.substring(0, longNodeName.lastIndexOf("."))).orElse(null);
            YamlConfiguration.YamlBuilder.Shadow newShadow = null;

            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                newShadow = shadowLeaf(longNodeName, nodeName, shadowParent);
            }
            else if (isNodeCollection(value)) {
                final int count = handler.getChildrenCount(parent, nodeName);
                newShadow = shadowCollection(longNodeName, nodeName, count, shadowParent);
            }
            else if (value instanceof Map) {
                newShadow = shadowMap(longNodeName, nodeName, shadowParent);
            }
            else {
                newShadow = shadowLeaf(longNodeName, nodeName, shadowParent);
            }

            documentShadow.add(newShadow);
            return newShadow;
        }

        String toLongName(ImmutableNode node, ReferenceNodeHandler handler, String init) {
            if(node != null) {
                return toLongName(handler.getParent(node), handler, node.getNodeName().replaceAll("\\.","\\^") +"."+ init);
            }
            else {
                return init;
            }
        }

        @Override
        protected void update(ImmutableNode immutableNode, Object o, ReferenceNodeHandler referenceNodeHandler) {
        }

        Map<String, Object> getDocument() {
            return document;
        }

        Optional<Shadow> by(String longName) {
            return documentShadow.stream().filter(s->s.longName.equals(longName)).findFirst();
        }

        Shadow shadowMap(String longName, String name, Shadow parent) {
            return new ShadowMapNode(longName, name,parent);
        }

        Shadow shadowLeaf(String longName, String name, Shadow parent) {
            return new ShadowLeafNode(longName, name,parent);
        }

        Shadow shadowCollection(String longName, String name, int size, Shadow parent) {
            return new ShadowCollectionNode(longName, name,size,parent);
        }

        abstract class Shadow<T>{
            String name;
            String longName;
            Shadow parent;
            T reference;
            int pointer = 0;


            Shadow(String longName, String name, Shadow parent) {
                this.name = name;
                this.longName = longName;
                this.parent = parent;
            }

            void syncDocument(Object value) {
                if(this.parent instanceof ShadowCollectionNode){
                    ((ShadowCollectionNode)parent).reference.get(pointer).put(name,value);
                    pointer++;
                }
                else {
                    ShadowMapNode parentMap = (ShadowMapNode)this.parent;
                    parentMap.reference.put(name,value);
                }
            }
        }

        class ShadowCollectionNode extends Shadow<List<Map<String,Object>>> {

            ShadowCollectionNode(String longName, String name, int size, Shadow parent) {
                super(longName, name,parent);
                this.reference = new ArrayList<>();
                IntStream.range(0,size).forEach(i->this.reference.add(new HashMap<>()));

                if(parent instanceof ShadowMapNode) {
                    ((ShadowMapNode)parent).reference.put(name, this.reference);
                }
            }

            @Override
            void syncDocument(Object value) {
                // do nothing
            }
        }

        class ShadowMapNode extends Shadow<Map<String,Object>> {
            private int pointer = 0;

            ShadowMapNode(String longName, String name, Shadow parent) {
                super(longName, name,parent);

                if(parent == null){
                    this.reference = document;
                }
                else if(parent instanceof ShadowMapNode) {
                    this.reference = new HashMap<>();
                    ((ShadowMapNode)parent).reference.put(name,reference);
                }
                else if(parent instanceof ShadowCollectionNode) {
                    this.reference = new HashMap<>();
                    ((ShadowCollectionNode)parent).reference.get(pointer).put(name,this.reference);
                }
            }
        }

        class ShadowLeafNode extends Shadow<Object> {

            ShadowLeafNode(String longName, String name, Shadow parent) {
                super(longName, name,parent);
            }
        }
    }
}