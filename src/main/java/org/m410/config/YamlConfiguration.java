package org.m410.config;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.NodeModel;
import org.apache.commons.configuration2.tree.NodeTreeWalker;
import org.apache.commons.configuration2.tree.ReferenceNodeHandler;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Yaml configuration that can be used with <a href="https://commons.apache.org/proper/commons-configuration/">
 * Apache Common Configuration</a>.
 *
 * @author Michael Fortin
 */
public class YamlConfiguration extends BaseHierarchicalConfiguration implements FileBasedConfiguration {

    public YamlConfiguration() {
        super();
    }

    public YamlConfiguration(HierarchicalConfiguration<ImmutableNode> c) {
        super(c);
    }

    public YamlConfiguration(NodeModel<ImmutableNode> model) {
        super(model);
    }

    public void write(Writer out) throws ConfigurationException, IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        new Yaml(options).dump(fromNode(), out);
    }

    public void read(Reader in) throws ConfigurationException, IOException {
        Yaml yaml = new Yaml();
        ImmutableNode.Builder rootBuilder = new ImmutableNode.Builder();
        ImmutableNode root = rootBuilder.create();

        Map<ImmutableNode, ?> elemRefMap = toNodeMap(yaml.loadAll(in).iterator().next());
        ImmutableNode top = addChildrenToRoot(root, elemRefMap);
        getSubConfigurationParentModel().mergeRoot(top, "yaml", elemRefMap, null, this);
    }

    ImmutableNode addChildrenToRoot(ImmutableNode root, Map<ImmutableNode, ?> elemRefMap) {
        ImmutableNode top = root;

        for (ImmutableNode node : elemRefMap.keySet()) {
            if (elemRefMap.get(node) instanceof Map) {
                top = top.addChild(addChildrenToRoot(node, (Map) elemRefMap.get(node)));
            }
            else if (isNodeCollection(elemRefMap.get(node))) {
                Collection<Map<ImmutableNode, ?>> n = (Collection<Map<ImmutableNode, ?>>) elemRefMap.get(node);

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

    private static boolean isNodeCollection(Object o) {
        // making some assumptions that all elements are the same
        return o instanceof Collection &&
               ((Collection) o).size() > 0 &&
               ((Collection) o).iterator().next() instanceof Map;
    }

    private Map<String, Object> fromNode() {
        final YamlBuilder visitor = new YamlBuilder();
        NodeTreeWalker.INSTANCE.walkBFS(getNodeModel().getRootNode(), visitor, getNodeModel().getNodeHandler());
        return visitor.getDocument();
    }

    Map<ImmutableNode, ?> toNodeMap(Object load) {
        final Map<String, ?> map = (Map<String, ?>) load;
        final Map<ImmutableNode, Object> nodeMap = new HashMap<>();

        for (String key : map.keySet()) {
            Object value = map.get(key);

            if (value instanceof Map) {
                ImmutableNode currentNode = new ImmutableNode.Builder().name(key).value(value).create();
                nodeMap.put(currentNode, toNodeMap(value));
            }
            else if (isNodeCollection(value)) {
                ImmutableNode currentNode = new ImmutableNode.Builder().name(key).value(value).create();
                Collection<Map<String, ?>> valueMaps = (Collection<Map<String, ?>>) value;
                nodeMap.put(currentNode, valueMaps.stream().map(vm -> toNodeMap(vm)).collect(Collectors.toList()));
            }
            else {
                // not sure if this is good idea, key doesn't output if value is null.  setting to empty string
                // will fix that, but it's not accurate.
                Object outVal = (value != null ? value : "");
                ImmutableNode currentNode = new ImmutableNode.Builder().name(key).value(outVal).create();
                nodeMap.put(currentNode, outVal);
            }
        }

        return nodeMap;
    }

    /**
     * This has to be here, because it's overriding an inner class in the superclass.
     */
    private final class YamlBuilder extends BuilderVisitor {
        final Map<String, Object> document = new TreeMap<>();
        final List<Shadow> documentShadow = new ArrayList<>();

        YamlBuilder() {
            super();
            documentShadow.add(new ShadowMapNode("yaml", "yaml", null));
        }

        @Override
        protected void insert(final ImmutableNode node, final ImmutableNode parent,
                final ImmutableNode siblingBefore, final ImmutableNode siblingAfter,
                final ReferenceNodeHandler handler) {

            final String nodeName = node.getNodeName();
            final String nodeNameEscaped = node.getNodeName().replaceAll("\\.", "\\^");
            final String longNodeName = toLongName(parent, handler, nodeNameEscaped);
            final Object value = node.getValue();

            final Optional<Shadow> by = by(longNodeName);
            Shadow shadow = by.orElseGet(() -> makeShadow(parent, handler, nodeName, longNodeName, value));
            shadow.syncDocument(value);
        }

        @Override
        protected void update(ImmutableNode node, Object value, ReferenceNodeHandler handler) {
            final ImmutableNode parent = handler.getParent(node);
            final String nodeName = node.getNodeName();
            final String nodeNameEscaped = node.getNodeName().replaceAll("\\.", "\\^");
            final String longNodeName = toLongName(parent, handler, nodeNameEscaped);

            final Optional<Shadow> by = by(longNodeName);
            Shadow shadow = by.orElseGet(() -> makeShadow(parent, handler, nodeName, longNodeName, value));
            shadow.syncDocument(value);
        }

        Shadow makeShadow(ImmutableNode parent, ReferenceNodeHandler handler,
                String nodeName, String longNodeName, Object value) {
            final Shadow shadowParent = by(longNodeName.substring(0, longNodeName.lastIndexOf("."))).orElse(null);
            final Shadow newShadow;

            if (isNodeCollection(value)) {
                final int count = handler.getChildrenCount(parent, nodeName);
                newShadow = shadowCollection(longNodeName, nodeName, count, shadowParent);
            }
            else if (value instanceof Map) {
                newShadow = shadowMap(longNodeName, nodeName, shadowParent);
            }
            else { // list of strings or any raw data type
                newShadow = shadowLeaf(longNodeName, nodeName, shadowParent);
            }

            documentShadow.add(newShadow);
            return newShadow;
        }

        String toLongName(ImmutableNode node, ReferenceNodeHandler handler, String init) {
            if (node != null) {
                final String nodePath = node.getNodeName().replaceAll("\\.", "\\^") + "." + init;
                return toLongName(handler.getParent(node), handler, nodePath);
            }
            else {
                return init;
            }
        }

        Map<String, Object> getDocument() {
            return document;
        }

        Optional<Shadow> by(String longName) {
            return documentShadow.stream().filter(s -> s.longName.equals(longName)).findFirst();
        }

        Shadow shadowMap(String longName, String name, Shadow parent) {
            return new ShadowMapNode(longName, name, parent);
        }

        Shadow shadowLeaf(String longName, String name, Shadow parent) {
            return new ShadowLeafNode(longName, name, parent);
        }

        Shadow shadowCollection(String longName, String name, int size, Shadow parent) {
            return new ShadowCollectionNode(longName, name, size, parent);
        }

        /**
         * @param <T> the type of the node shadow
         */
        abstract class Shadow<T> {
            final String name;
            final String longName;
            final Shadow parent;
            final T reference;
            int pointer = 0;


            Shadow(String longName, String name, Shadow parent, T reference) {
                this.name = name;
                this.longName = longName;
                this.parent = parent;
                this.reference = reference;
            }

            abstract void syncDocument(Object value);

            @Override
            public String toString() {
                return "Shadow@" + this.hashCode() + "{" +
                       "longName='" + longName + '\'' +
                       '}';
            }
        }

        final class ShadowCollectionNode extends Shadow<List<Map<String, Object>>> {

            ShadowCollectionNode(String longName, String name, int size, Shadow parent) {
                super(longName, name, parent, new ArrayList<>());
                IntStream.range(0, size).forEach(i -> this.reference.add(new TreeMap<>()));

                if (parent instanceof ShadowMapNode) {
                    ((ShadowMapNode) parent).reference.put(name, this.reference);
                }
                else {
                    throw new RuntimeException("list of list not implemented");
                }
            }

            @Override
            void syncDocument(Object value) { // do nothing
            }

            @Override
            public String toString() {
                return "Collection" + super.toString();
            }
        }

        final class ShadowMapNode extends Shadow<Map<String, Object>> {

            ShadowMapNode(String longName, String name, Shadow parent) {
                super(longName, name, parent, parent == null ? document : new TreeMap<>());

                if (parent instanceof ShadowMapNode) {
                    ((ShadowMapNode) parent).reference.put(name, this.reference);
                }
                else if (parent instanceof ShadowCollectionNode) {
                    ((ShadowCollectionNode) parent).reference.get(pointer).put(name, this.reference);
                    pointer++;
                }
            }

            @Override
            void syncDocument(Object value) { // do nothing
            }

            @Override
            public String toString() {
                return "Map" + super.toString();
            }
        }

        final class ShadowLeafNode extends Shadow<Object> {

            ShadowLeafNode(String longName, String name, Shadow parent) {
                super(longName, name, parent, null);
            }

            @Override
            void syncDocument(Object value) {
                if (this.parent instanceof ShadowCollectionNode) {
                    final ShadowCollectionNode parent = (ShadowCollectionNode) this.parent;

                    if (parent.reference.size() <= pointer) {
                        parent.reference.add(pointer, new TreeMap<>());
                    }

                    parent.reference.get(pointer).put(name, value);
                    pointer++;
                }
                else {
                    ((ShadowMapNode) this.parent).reference.put(name, value);
                }
            }

            @Override
            public String toString() {
                return "Leaf" + super.toString();
            }
        }
    }
}